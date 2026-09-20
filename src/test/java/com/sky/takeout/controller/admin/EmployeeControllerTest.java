package com.sky.takeout.controller.admin;

import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.service.EmployeeService;
import com.sky.takeout.vo.EmployeeDetailVO;
import com.sky.takeout.vo.EmployeeLoginVO;
import com.sky.takeout.vo.EmployeeVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void shouldReturnWrappedPageResult() throws Exception {
        EmployeeVO employee = new EmployeeVO(
                1L,
                "Admin",
                "admin",
                "13800000001",
                1,
                1,
                LocalDateTime.of(2026, 9, 19, 10, 0)
        );

        when(employeeService.pageQuery(any(EmployeePageQueryDTO.class)))
                .thenReturn(new PageResult<>(1, List.of(employee)));

        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "Admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));
    }

    @Test
    void shouldLoginEmployee() throws Exception {
        when(employeeService.login(any())).thenReturn(new EmployeeLoginVO(
                1L,
                "admin",
                "Admin",
                "jwt-token"
        ));

        mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void shouldRejectInvalidPage() throws Exception {
        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(
                        "page must be greater than or equal to 1"
                ))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldRejectNonNumericPage() throws Exception {
        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "abc")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Invalid value for parameter: page"
                ));
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        when(employeeService.create(any())).thenReturn(3L);

        mockMvc.perform(post("/admin/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Admin",
                                  "username": "new-admin",
                                  "password": "secret123",
                                  "phone": "13800000003",
                                  "sex": 1,
                                  "idNumber": "110101199003030033"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/admin/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "username": "new-admin",
                                  "password": "123",
                                  "phone": "invalid",
                                  "sex": 3,
                                  "idNumber": "invalid"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldGetEmployeeDetail() throws Exception {
        EmployeeDetailVO employee = new EmployeeDetailVO(
                1L,
                "Admin",
                "admin",
                "13800000001",
                1,
                "110101199001010011",
                1,
                LocalDateTime.of(2026, 9, 19, 10, 0),
                LocalDateTime.of(2026, 9, 19, 10, 0)
        );
        when(employeeService.getById(1L)).thenReturn(employee);

        mockMvc.perform(get("/admin/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.idNumber").value("110101199001010011"));
    }

    @Test
    void shouldReturnNotFoundForMissingEmployee() throws Exception {
        when(employeeService.getById(99L))
                .thenThrow(new BusinessException(404, "Employee not found"));

        mockMvc.perform(get("/admin/employee/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        doNothing().when(employeeService).update(any());

        mockMvc.perform(put("/admin/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "name": "Updated Admin",
                                  "username": "admin",
                                  "phone": "13800000001",
                                  "sex": 1,
                                  "idNumber": "110101199001010011"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldUpdateEmployeeStatusAndDeleteEmployee() throws Exception {
        doNothing().when(employeeService).updateStatus(1L, 0);
        doNothing().when(employeeService).delete(1L);

        mockMvc.perform(post("/admin/employee/status/0")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/admin/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
