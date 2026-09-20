package com.sky.takeout.mapper;

import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    long countByQuery(EmployeePageQueryDTO query);

    List<Employee> selectPage(EmployeePageQueryDTO query);

    long countByUsername(
            @Param("username") String username,
            @Param("excludeId") Long excludeId
    );

    long countByIdNumber(
            @Param("idNumber") String idNumber,
            @Param("excludeId") Long excludeId
    );

    int insert(Employee employee);

    Employee selectByUsername(String username);

    Employee selectById(Long id);

    int update(Employee employee);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updateUser") Long updateUser
    );

    int deleteById(Long id);
}
