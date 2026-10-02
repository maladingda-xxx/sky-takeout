package com.sky.mapper;

import com.sky.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    @Select("select * from employee where id = #{id}")
    Employee selectById(Long id);
}
