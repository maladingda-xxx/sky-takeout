package com.sky.takeout.controller.admin;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.EmployeeCreateDTO;
import com.sky.takeout.dto.EmployeeLoginDTO;
import com.sky.takeout.dto.EmployeePageQueryDTO;
import com.sky.takeout.dto.EmployeeUpdateDTO;
import com.sky.takeout.service.EmployeeService;
import com.sky.takeout.vo.EmployeeDetailVO;
import com.sky.takeout.vo.EmployeeLoginVO;
import com.sky.takeout.vo.EmployeeVO;
import com.sky.takeout.vo.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/page")
    public Result<PageResult<EmployeeVO>> page(@Valid EmployeePageQueryDTO query) {
        return Result.success(employeeService.pageQuery(query));
    }

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(
            @Valid @RequestBody EmployeeLoginDTO employeeLoginDTO
    ) {
        return Result.success(employeeService.login(employeeLoginDTO));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody EmployeeCreateDTO employeeCreateDTO) {
        return Result.success(employeeService.create(employeeCreateDTO));
    }

    @GetMapping("/{id}")
    public Result<EmployeeDetailVO> getById(@PathVariable @Positive Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody EmployeeUpdateDTO employeeUpdateDTO) {
        employeeService.update(employeeUpdateDTO);
        return Result.success(null);
    }

    @PostMapping("/status/{status}")
    public Result<Void> updateStatus(
            @PathVariable @Min(0) @Max(1) Integer status,
            @RequestParam @Positive Long id
    ) {
        employeeService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Long id) {
        employeeService.delete(id);
        return Result.success(null);
    }
}
