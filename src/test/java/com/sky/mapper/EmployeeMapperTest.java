package com.sky.mapper;

import com.sky.pojo.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集成测试：@SpringBootTest 会启动整个 Spring 容器，
 * 这样 EmployeeMapper 才是"真"的（连着数据库），而不是假对象。
 */
@SpringBootTest
class EmployeeMapperTest {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Test
    void testSelectById() {
        Employee emp = employeeMapper.selectById(1L);
        System.out.println("查询结果: " + emp);
        Assertions.assertNotNull(emp, "应该能查到 id=1 的员工");
        Assertions.assertEquals("张三", emp.getName());
    }
}
