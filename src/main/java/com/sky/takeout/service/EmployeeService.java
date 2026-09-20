package com.sky.takeout.service;

import com.sky.takeout.dto.EmployeeCreateDTO;
import com.sky.takeout.dto.EmployeeLoginDTO;
import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.dto.EmployeeUpdateDTO;
import com.sky.takeout.vo.EmployeeDetailVO;
import com.sky.takeout.vo.EmployeeLoginVO;
import com.sky.takeout.vo.EmployeeVO;
import com.sky.takeout.vo.PageResult;

public interface EmployeeService {

    PageResult<EmployeeVO> pageQuery(EmployeePageQueryDTO query);

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    Long create(EmployeeCreateDTO employeeCreateDTO);

    EmployeeDetailVO getById(Long id);

    void update(EmployeeUpdateDTO employeeUpdateDTO);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
