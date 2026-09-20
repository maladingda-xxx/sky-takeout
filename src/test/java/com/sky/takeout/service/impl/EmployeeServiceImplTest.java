package com.sky.takeout.service.impl;

import com.sky.takeout.common.CurrentUserContext;
import com.sky.takeout.dto.EmployeeCreateDTO;
import com.sky.takeout.dto.EmployeeLoginDTO;
import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.dto.EmployeeUpdateDTO;
import com.sky.takeout.entity.Employee;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.EmployeeMapper;
import com.sky.takeout.vo.EmployeeVO;
import com.sky.takeout.vo.EmployeeLoginVO;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @AfterEach
    void clearCurrentUser() {
        CurrentUserContext.clear();
    }

    @Test
    void shouldReturnEmptyPageWithoutSelectingRows() {
        EmployeePageQueryDTO query = new EmployeePageQueryDTO();
        when(employeeMapper.countByQuery(query)).thenReturn(0L);

        PageResult<EmployeeVO> result = employeeService.pageQuery(query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(employeeMapper, never()).selectPage(query);
    }

    @Test
    void shouldNormalizeQueryAndMapEmployeeToVO() {
        EmployeePageQueryDTO query = new EmployeePageQueryDTO();
        query.setPage(0);
        query.setPageSize(500);
        query.setName("  Admin  ");

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Admin");
        employee.setUsername("admin");
        employee.setPhone("13800000001");
        employee.setSex(1);
        employee.setStatus(1);
        employee.setUpdateTime(LocalDateTime.of(2026, 9, 19, 12, 0));

        when(employeeMapper.countByQuery(query)).thenReturn(1L);
        when(employeeMapper.selectPage(query)).thenReturn(List.of(employee));

        PageResult<EmployeeVO> result = employeeService.pageQuery(query);

        ArgumentCaptor<EmployeePageQueryDTO> captor = ArgumentCaptor.forClass(EmployeePageQueryDTO.class);
        verify(employeeMapper).countByQuery(captor.capture());

        EmployeePageQueryDTO normalizedQuery = captor.getValue();
        assertEquals(1, normalizedQuery.getPage());
        assertEquals(100, normalizedQuery.getPageSize());
        assertEquals("Admin", normalizedQuery.getName());
        assertEquals(1, result.getTotal());
        assertEquals("admin", result.getRecords().get(0).getUsername());
    }

    @Test
    void shouldCreateEmployeeWithEncodedPassword() {
        EmployeeCreateDTO employeeCreateDTO = new EmployeeCreateDTO();
        employeeCreateDTO.setName("New Admin");
        employeeCreateDTO.setUsername("new-admin");
        employeeCreateDTO.setPassword("secret123");
        employeeCreateDTO.setPhone("13800000003");
        employeeCreateDTO.setSex(1);
        employeeCreateDTO.setIdNumber("110101199003030033");

        when(employeeMapper.countByUsername("new-admin", null)).thenReturn(0L);
        when(employeeMapper.countByIdNumber("110101199003030033", null)).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(employeeMapper.insert(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(3L);
            return 1;
        });

        Long id = employeeService.create(employeeCreateDTO);

        assertEquals(3L, id);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).insert(captor.capture());

        Employee employee = captor.getValue();
        assertEquals("encoded-password", employee.getPassword());
        assertEquals(1, employee.getStatus());
        assertEquals(1L, employee.getCreateUser());
        assertEquals(1L, employee.getUpdateUser());
    }

    @Test
    void shouldRejectDuplicateUsernameBeforeInsert() {
        EmployeeCreateDTO employeeCreateDTO = new EmployeeCreateDTO();
        employeeCreateDTO.setName("Admin");
        employeeCreateDTO.setUsername("admin");
        employeeCreateDTO.setPassword("secret123");
        employeeCreateDTO.setPhone("13800000003");
        employeeCreateDTO.setSex(1);
        employeeCreateDTO.setIdNumber("110101199003030033");

        when(employeeMapper.countByUsername("admin", null)).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.create(employeeCreateDTO)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Username already exists", exception.getMessage());
        verify(employeeMapper, never()).insert(any(Employee.class));
    }

    @Test
    void shouldRejectUpdateWhenEmployeeDoesNotExist() {
        EmployeeUpdateDTO employeeUpdateDTO = new EmployeeUpdateDTO();
        employeeUpdateDTO.setId(99L);

        when(employeeMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.update(employeeUpdateDTO)
        );

        assertEquals(404, exception.getCode());
        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void shouldLoginWithValidCredentials() {
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password");

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Admin");
        employee.setUsername("admin");
        employee.setPassword("encoded-password");
        employee.setStatus(1);

        when(employeeMapper.selectByUsername("admin")).thenReturn(employee);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(1L)).thenReturn("jwt-token");

        EmployeeLoginVO result = employeeService.login(loginDTO);

        assertEquals(1L, result.getId());
        assertEquals("admin", result.getUsername());
        assertEquals("Admin", result.getName());
        assertEquals("jwt-token", result.getToken());
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        EmployeeLoginDTO loginDTO = new EmployeeLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrong");

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setUsername("admin");
        employee.setPassword("encoded-password");
        employee.setStatus(1);

        when(employeeMapper.selectByUsername("admin")).thenReturn(employee);
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.login(loginDTO)
        );

        assertEquals(401, exception.getCode());
    }

    @Test
    void shouldPreventDisablingCurrentEmployee() {
        CurrentUserContext.setUserId(1L);
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeMapper.selectById(1L)).thenReturn(employee);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.updateStatus(1L, 0)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Cannot disable current employee", exception.getMessage());
        verify(employeeMapper, never()).updateStatus(any(), any(), any());
    }

    @Test
    void shouldPreventDeletingCurrentEmployee() {
        CurrentUserContext.setUserId(1L);
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeMapper.selectById(1L)).thenReturn(employee);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.delete(1L)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Cannot delete current employee", exception.getMessage());
        verify(employeeMapper, never()).deleteById(1L);
    }
}
