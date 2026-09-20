package com.sky.takeout.service;

import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.AddressBookUpdateDTO;
import com.sky.takeout.vo.AddressBookVO;

import java.util.List;

public interface AddressBookService {

    AddressBookVO create(AddressBookCreateDTO createDTO);

    List<AddressBookVO> list();

    AddressBookVO getById(Long id);

    AddressBookVO getDefault();

    void update(AddressBookUpdateDTO updateDTO);

    void setDefault(Long id);

    void delete(Long id);
}
