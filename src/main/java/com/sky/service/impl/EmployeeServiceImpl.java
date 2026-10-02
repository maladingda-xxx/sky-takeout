package com.sky.service.impl;

import com.sky.service.EmployeeService;
import com.sky.mapper.EmployeeMapper;
import com.sky.pojo.Employee;
import org.springframework.stereotype.Service;

/**
 * 员工业务实现。
 * @Service 把它注册成Spring Bean,交给容器管理。
 */

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper employeeMapper;
    public EmployeeServiceImpl(EmployeeMapper employeeMapper){
        this.employeeMapper=employeeMapper;
    }
    @Override
    public Employee getById(Long id){
        return employeeMapper.selectById(id);
    }
}
