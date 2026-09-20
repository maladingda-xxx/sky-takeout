package com.sky.takeout.controller.admin;

import com.sky.takeout.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonController.class)
class CommonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void shouldUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.png",
                "image/png",
                "image-content".getBytes()
        );
        when(fileStorageService.uploadImage(any())).thenReturn(
                "/uploads/0123456789abcdef0123456789abcdef.png"
        );

        mockMvc.perform(multipart("/admin/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(
                        "/uploads/0123456789abcdef0123456789abcdef.png"
                ));
    }
}
