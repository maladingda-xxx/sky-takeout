package com.sky.takeout.mapper;

import com.sky.takeout.dto.EmployeeCreateDTO;
import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.dto.EmployeeUpdateDTO;
import com.sky.takeout.entity.Employee;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.EmployeeService;
import com.sky.takeout.vo.EmployeeDetailVO;
import com.sky.takeout.vo.EmployeeVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeMapperIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldFilterAndPageEmployees() {
        EmployeePageQueryDTO query = new EmployeePageQueryDTO();
        query.setPage(1);
        query.setPageSize(1);
        query.setStatus(1);

        PageResult<EmployeeVO> result = employeeService.pageQuery(query);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("admin", result.getRecords().get(0).getUsername());
    }

    @Test
    void shouldSearchByNameAndMapDatabaseRowToVo() {
        EmployeePageQueryDTO query = new EmployeePageQueryDTO();
        query.setName("per");

        PageResult<EmployeeVO> result = employeeService.pageQuery(query);

        assertEquals(1, result.getTotal());
        EmployeeVO employee = result.getRecords().get(0);
        assertEquals("Operator", employee.getName());
        assertEquals("operator", employee.getUsername());
        assertEquals(0, employee.getStatus());
    }

    @Test
    void shouldReturnEmptyResultWhenNoEmployeeMatches() {
        EmployeePageQueryDTO query = new EmployeePageQueryDTO();
        query.setName("not-found");

        PageResult<EmployeeVO> result = employeeService.pageQuery(query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void shouldCreateEmployeeAndEncryptPassword() {
        EmployeeCreateDTO employeeCreateDTO = createEmployeeDTO(
                "new-admin",
                "110101199003030033"
        );

        Long id = employeeService.create(employeeCreateDTO);

        Employee employee = employeeMapper.selectById(id);
        assertEquals("new-admin", employee.getUsername());
        assertNotEquals("secret123", employee.getPassword());
        assertTrue(passwordEncoder.matches("secret123", employee.getPassword()));
        assertEquals(1, employee.getStatus());
    }

    @Test
    void shouldUpdateEmployeeAndStatus() {
        Long id = employeeService.create(createEmployeeDTO(
                "update-user",
                "110101199004040044"
        ));

        EmployeeUpdateDTO updateDTO = new EmployeeUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setName("Updated User");
        updateDTO.setUsername("updated-user");
        updateDTO.setPhone("13800000004");
        updateDTO.setSex(2);
        updateDTO.setIdNumber("110101199004040044");

        employeeService.update(updateDTO);
        employeeService.updateStatus(id, 0);

        EmployeeDetailVO employee = employeeService.getById(id);
        assertEquals("Updated User", employee.getName());
        assertEquals("updated-user", employee.getUsername());
        assertEquals(0, employee.getStatus());
    }

    @Test
    void shouldDeleteEmployee() {
        Long id = employeeService.create(createEmployeeDTO(
                "delete-user",
                "110101199005050055"
        ));

        employeeService.delete(id);

        assertNull(employeeMapper.selectById(id));
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.getById(id)
        );
        assertEquals(404, exception.getCode());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> employeeService.create(createEmployeeDTO(
                        "admin",
                        "110101199006060066"
                ))
        );

        assertEquals(409, exception.getCode());
        assertEquals("Username already exists", exception.getMessage());
    }

    private EmployeeCreateDTO createEmployeeDTO(String username, String idNumber) {
        EmployeeCreateDTO employeeCreateDTO = new EmployeeCreateDTO();
        employeeCreateDTO.setName("Test Employee");
        employeeCreateDTO.setUsername(username);
        employeeCreateDTO.setPassword("secret123");
        employeeCreateDTO.setPhone("13800000009");
        employeeCreateDTO.setSex(1);
        employeeCreateDTO.setIdNumber(idNumber);
        return employeeCreateDTO;
    }
}
