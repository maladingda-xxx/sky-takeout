package com.sky.takeout.controller.admin;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.DishPageQueryDTO;
import com.sky.takeout.dto.DishUpdateDTO;
import com.sky.takeout.service.DishService;
import com.sky.takeout.vo.DishDetailVO;
import com.sky.takeout.vo.DishPageVO;
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
@RequestMapping("/admin/dish")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/page")
    public Result<PageResult<DishPageVO>> page(@Valid DishPageQueryDTO query) {
        return Result.success(dishService.pageQuery(query));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody DishCreateDTO dishCreateDTO) {
        return Result.success(dishService.create(dishCreateDTO));
    }

    @GetMapping("/{id}")
    public Result<DishDetailVO> getById(@PathVariable @Positive Long id) {
        return Result.success(dishService.getById(id));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody DishUpdateDTO dishUpdateDTO) {
        dishService.update(dishUpdateDTO);
        return Result.success(null);
    }

    @PostMapping("/status/{status}")
    public Result<Void> updateStatus(
            @PathVariable @Min(0) @Max(1) Integer status,
            @RequestParam @Positive Long id
    ) {
        dishService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Long id) {
        dishService.delete(id);
        return Result.success(null);
    }
}
