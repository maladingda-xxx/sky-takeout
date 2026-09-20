package com.sky.takeout.controller.user;

import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.vo.AddressBookVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressBookController.class)
class AddressBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressBookService addressBookService;

    @Test
    void shouldCreateAddress() throws Exception {
        when(addressBookService.create(any())).thenReturn(addressVO(1L, 1));

        mockMvc.perform(post("/user/addressBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAddressJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consignee").value("Test User"))
                .andExpect(jsonPath("$.data.isDefault").value(1));
    }

    @Test
    void shouldListAndGetAddresses() throws Exception {
        when(addressBookService.list()).thenReturn(List.of(addressVO(1L, 1)));
        when(addressBookService.getById(1L)).thenReturn(addressVO(1L, 1));
        when(addressBookService.getDefault()).thenReturn(addressVO(1L, 1));

        mockMvc.perform(get("/user/addressBook/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));

        mockMvc.perform(get("/user/addressBook/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/addressBook/default"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateSetDefaultAndDeleteAddress() throws Exception {
        doNothing().when(addressBookService).update(any());
        doNothing().when(addressBookService).setDefault(1L);
        doNothing().when(addressBookService).delete(1L);

        mockMvc.perform(put("/user/addressBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "consignee": "Updated User",
                                  "sex": 2,
                                  "phone": "13800000009",
                                  "provinceName": "Shanghai",
                                  "cityName": "Shanghai",
                                  "districtName": "Pudong",
                                  "detail": "No. 2 Test Road",
                                  "label": "Office",
                                  "isDefault": 1
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/user/addressBook/default/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/user/addressBook/1"))
                .andExpect(status().isOk());
    }

    private String validAddressJson() {
        return """
                {
                  "consignee": "Test User",
                  "sex": 1,
                  "phone": "13800000009",
                  "provinceName": "Shanghai",
                  "cityName": "Shanghai",
                  "districtName": "Pudong",
                  "detail": "No. 1 Test Road",
                  "label": "Home",
                  "isDefault": 1
                }
                """;
    }

    private AddressBookVO addressVO(Long id, int isDefault) {
        return new AddressBookVO(
                id,
                "Test User",
                1,
                "13800000009",
                "Shanghai",
                "Shanghai",
                "Pudong",
                "No. 1 Test Road",
                "Home",
                isDefault
        );
    }
}
