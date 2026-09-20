package com.sky.takeout.service.impl;

import com.sky.takeout.common.CurrentUserContext;
import com.sky.takeout.dto.EmployeeCreateDTO;
import com.sky.takeout.dto.EmployeeLoginDTO;
import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.dto.EmployeeUpdateDTO;
import com.sky.takeout.entity.Employee;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.EmployeeMapper;
import com.sky.takeout.service.EmployeeService;
import com.sky.takeout.vo.EmployeeDetailVO;
import com.sky.takeout.vo.EmployeeLoginVO;
import com.sky.takeout.vo.EmployeeVO;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.utils.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int ENABLED_STATUS = 1;

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public EmployeeServiceImpl(
            EmployeeMapper employeeMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public PageResult<EmployeeVO> pageQuery(EmployeePageQueryDTO query) {
        normalizeQuery(query);

        long total = employeeMapper.countByQuery(query);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }

        List<EmployeeVO> records = employeeMapper.selectPage(query).stream()
                .map(this::toVO)
                .toList();

        return new PageResult<>(total, records);
    }

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername().trim();
        Employee employee = employeeMapper.selectByUsername(username);

        if (employee == null
                || employee.getStatus() != ENABLED_STATUS
                || !passwordEncoder.matches(
                        employeeLoginDTO.getPassword(),
                        employee.getPassword()
                )) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid username or password"
            );
        }

        String token = jwtUtil.generateToken(employee.getId());
        return new EmployeeLoginVO(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                token
        );
    }

    @Override
    @Transactional
    public Long create(EmployeeCreateDTO employeeCreateDTO) {
        String username = employeeCreateDTO.getUsername().trim();
        String idNumber = employeeCreateDTO.getIdNumber().trim();

        validateUnique(
                username,
                idNumber,
                null
        );

        Employee employee = new Employee();
        employee.setName(employeeCreateDTO.getName().trim());
        employee.setUsername(username);
        employee.setPassword(passwordEncoder.encode(employeeCreateDTO.getPassword()));
        employee.setPhone(employeeCreateDTO.getPhone());
        employee.setSex(employeeCreateDTO.getSex());
        employee.setIdNumber(idNumber);
        employee.setStatus(ENABLED_STATUS);
        long currentUserId = CurrentUserContext.getUserIdOrDefault();
        employee.setCreateUser(currentUserId);
        employee.setUpdateUser(currentUserId);

        try {
            if (employeeMapper.insert(employee) != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to create employee"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Username or ID number already exists"
            );
        }

        return employee.getId();
    }

    @Override
    public EmployeeDetailVO getById(Long id) {
        return toDetailVO(requireEmployee(id));
    }

    @Override
    @Transactional
    public void update(EmployeeUpdateDTO employeeUpdateDTO) {
        requireEmployee(employeeUpdateDTO.getId());

        String username = employeeUpdateDTO.getUsername().trim();
        String idNumber = employeeUpdateDTO.getIdNumber().trim();

        validateUnique(
                username,
                idNumber,
                employeeUpdateDTO.getId()
        );

        Employee employee = new Employee();
        employee.setId(employeeUpdateDTO.getId());
        employee.setName(employeeUpdateDTO.getName().trim());
        employee.setUsername(username);
        employee.setPhone(employeeUpdateDTO.getPhone());
        employee.setSex(employeeUpdateDTO.getSex());
        employee.setIdNumber(idNumber);
        employee.setUpdateUser(CurrentUserContext.getUserIdOrDefault());

        try {
            if (employeeMapper.update(employee) != 1) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to update employee"
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Username or ID number already exists"
            );
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Status must be 0 or 1"
            );
        }

        requireEmployee(id);

        long currentUserId = CurrentUserContext.getUserIdOrDefault();
        if (id == currentUserId && status == 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Cannot disable current employee"
            );
        }

        if (employeeMapper.updateStatus(id, status, currentUserId) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to update employee status"
            );
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireEmployee(id);

        if (id == CurrentUserContext.getUserIdOrDefault()) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Cannot delete current employee"
            );
        }

        if (employeeMapper.deleteById(id) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to delete employee"
            );
        }
    }

    private void normalizeQuery(EmployeePageQueryDTO query) {
        query.setPage(Math.max(query.getPage(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));

        if (query.getName() != null) {
            String name = query.getName().trim();
            query.setName(name.isEmpty() ? null : name);
        }
    }

    private EmployeeVO toVO(Employee employee) {
        return new EmployeeVO(
                employee.getId(),
                employee.getName(),
                employee.getUsername(),
                employee.getPhone(),
                employee.getSex(),
                employee.getStatus(),
                employee.getUpdateTime()
        );
    }

    private EmployeeDetailVO toDetailVO(Employee employee) {
        return new EmployeeDetailVO(
                employee.getId(),
                employee.getName(),
                employee.getUsername(),
                employee.getPhone(),
                employee.getSex(),
                employee.getIdNumber(),
                employee.getStatus(),
                employee.getCreateTime(),
                employee.getUpdateTime()
        );
    }

    private Employee requireEmployee(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Employee not found"
            );
        }
        return employee;
    }

    private void validateUnique(String username, String idNumber, Long excludeId) {
        if (employeeMapper.countByUsername(username, excludeId) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Username already exists"
            );
        }

        if (employeeMapper.countByIdNumber(idNumber, excludeId) > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "ID number already exists"
            );
        }
    }
}
