package com.sky.service;

import com.sky.pojo.Employee;

/**
 * 员工业务接口。
 * 注意：这里只声明“能做什么”，不关心“怎么做”。
 */

public interface EmployeeService {
    /**
     * 根据id查询员工
     */
    Employee getById(Long id);
}
