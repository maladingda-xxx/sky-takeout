package com.sky.takeout.mapper;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.AddressBookUpdateDTO;
import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.vo.AddressBookVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AddressBookIntegrationTest {

    @Autowired
    private AddressBookService addressBookService;

    @BeforeEach
    void setUpContext() {
        UserContext.setUserId(88L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldManageDefaultAddress() {
        AddressBookVO first = addressBookService.create(createDTO("Home", 0));
        AddressBookVO second = addressBookService.create(createDTO("Office", 0));

        assertEquals(1, first.getIsDefault());
        assertEquals(0, second.getIsDefault());
        assertEquals(2, addressBookService.list().size());

        addressBookService.setDefault(second.getId());
        assertEquals(second.getId(), addressBookService.getDefault().getId());

        AddressBookUpdateDTO updateDTO = new AddressBookUpdateDTO();
        updateDTO.setId(second.getId());
        updateDTO.setConsignee("Updated User");
        updateDTO.setSex(2);
        updateDTO.setPhone("13800000009");
        updateDTO.setProvinceName("Shanghai");
        updateDTO.setCityName("Shanghai");
        updateDTO.setDistrictName("Pudong");
        updateDTO.setDetail("Updated Road");
        updateDTO.setLabel("Office");
        updateDTO.setIsDefault(0);
        addressBookService.update(updateDTO);

        addressBookService.setDefault(second.getId());
        addressBookService.delete(second.getId());

        assertEquals(first.getId(), addressBookService.getDefault().getId());
    }

    private AddressBookCreateDTO createDTO(String label, int isDefault) {
        AddressBookCreateDTO dto = new AddressBookCreateDTO();
        dto.setConsignee("Test User");
        dto.setSex(1);
        dto.setPhone("13800000009");
        dto.setProvinceName("Shanghai");
        dto.setCityName("Shanghai");
        dto.setDistrictName("Pudong");
        dto.setDetail(label + " Road");
        dto.setLabel(label);
        dto.setIsDefault(isDefault);
        return dto;
    }
}
