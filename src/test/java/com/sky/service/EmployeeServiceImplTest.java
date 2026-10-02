package com.sky.service;

import com.sky.pojo.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmployeeServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void testGetById() {
        Employee emp = employeeService.getById(1L);
        System.out.println("Service 查询结果: " + emp);
        Assertions.assertNotNull(emp, "应该能查到 id=1 的员工");
        Assertions.assertEquals("张三", emp.getName());
    }
}
