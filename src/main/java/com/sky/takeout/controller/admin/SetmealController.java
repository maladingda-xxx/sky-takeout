package com.sky.takeout.controller.admin;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.SetmealCreateDTO;
import com.sky.takeout.dto.SetmealPageQueryDTO;
import com.sky.takeout.dto.SetmealUpdateDTO;
import com.sky.takeout.service.SetmealService;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;
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
@RequestMapping("/admin/setmeal")
public class SetmealController {

    private final SetmealService setmealService;

    public SetmealController(SetmealService setmealService) {
        this.setmealService = setmealService;
    }

    @GetMapping("/page")
    public Result<PageResult<SetmealPageVO>> page(@Valid SetmealPageQueryDTO query) {
        return Result.success(setmealService.pageQuery(query));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody SetmealCreateDTO setmealCreateDTO) {
        return Result.success(setmealService.create(setmealCreateDTO));
    }

    @GetMapping("/{id}")
    public Result<SetmealDetailVO> getById(@PathVariable @Positive Long id) {
        return Result.success(setmealService.getById(id));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody SetmealUpdateDTO setmealUpdateDTO) {
        setmealService.update(setmealUpdateDTO);
        return Result.success(null);
    }

    @PostMapping("/status/{status}")
    public Result<Void> updateStatus(
            @PathVariable @Min(0) @Max(1) Integer status,
            @RequestParam @Positive Long id
    ) {
        setmealService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Long id) {
        setmealService.delete(id);
        return Result.success(null);
    }
}
