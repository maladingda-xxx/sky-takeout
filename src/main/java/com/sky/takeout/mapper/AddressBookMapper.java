package com.sky.takeout.mapper;

import com.sky.takeout.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    List<AddressBook> selectByUserId(Long userId);

    AddressBook selectByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    AddressBook selectDefaultByUserId(Long userId);

    AddressBook selectFirstByUserId(Long userId);

    long countByUserId(Long userId);

    int insert(AddressBook addressBook);

    int update(AddressBook addressBook);

    int clearDefaultByUserId(Long userId);

    int setDefault(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    int deleteByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
}
