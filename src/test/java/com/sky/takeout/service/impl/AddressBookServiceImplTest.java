package com.sky.takeout.service.impl;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.entity.AddressBook;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.AddressBookMapper;
import com.sky.takeout.vo.AddressBookVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressBookServiceImplTest {

    @Mock
    private AddressBookMapper addressBookMapper;

    @InjectMocks
    private AddressBookServiceImpl addressBookService;

    @BeforeEach
    void setUpContext() {
        UserContext.setUserId(7L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldMakeFirstAddressDefault() {
        AddressBookCreateDTO createDTO = createDTO("Home");
        createDTO.setIsDefault(0);
        when(addressBookMapper.countByUserId(7L)).thenReturn(0L);
        when(addressBookMapper.insert(any(AddressBook.class))).thenAnswer(invocation -> {
            AddressBook address = invocation.getArgument(0);
            address.setId(1L);
            return 1;
        });

        AddressBookVO result = addressBookService.create(createDTO);

        assertEquals(1, result.getIsDefault());
        verify(addressBookMapper).clearDefaultByUserId(7L);
        verify(addressBookMapper).insert(any(AddressBook.class));
    }

    @Test
    void shouldKeepLaterAddressNonDefault() {
        AddressBookCreateDTO createDTO = createDTO("Office");
        when(addressBookMapper.countByUserId(7L)).thenReturn(1L);
        when(addressBookMapper.insert(any(AddressBook.class))).thenAnswer(invocation -> {
            AddressBook address = invocation.getArgument(0);
            address.setId(2L);
            return 1;
        });

        AddressBookVO result = addressBookService.create(createDTO);

        assertEquals(0, result.getIsDefault());
        verify(addressBookMapper, never()).clearDefaultByUserId(7L);
    }

    @Test
    void shouldRejectAddressOwnedByAnotherUser() {
        when(addressBookMapper.selectByIdAndUserId(99L, 7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> addressBookService.getById(99L)
        );

        assertEquals(404, exception.getCode());
    }

    @Test
    void shouldPromoteNextAddressWhenDefaultIsDeleted() {
        AddressBook existing = address(1L, 1);
        AddressBook next = address(2L, 0);
        when(addressBookMapper.selectByIdAndUserId(1L, 7L)).thenReturn(existing);
        when(addressBookMapper.deleteByIdAndUserId(1L, 7L)).thenReturn(1);
        when(addressBookMapper.selectFirstByUserId(7L)).thenReturn(next);

        addressBookService.delete(1L);

        verify(addressBookMapper).setDefault(2L, 7L);
    }

    private AddressBookCreateDTO createDTO(String label) {
        AddressBookCreateDTO dto = new AddressBookCreateDTO();
        dto.setConsignee("Test User");
        dto.setSex(1);
        dto.setPhone("13800000009");
        dto.setProvinceName("Shanghai");
        dto.setCityName("Shanghai");
        dto.setDistrictName("Pudong");
        dto.setDetail("No. 1 Test Road");
        dto.setLabel(label);
        return dto;
    }

    private AddressBook address(Long id, int isDefault) {
        AddressBook address = new AddressBook();
        address.setId(id);
        address.setUserId(7L);
        address.setIsDefault(isDefault);
        return address;
    }
}
