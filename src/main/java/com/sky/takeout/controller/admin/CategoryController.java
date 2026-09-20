package com.sky.takeout.controller.admin;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.CategoryCreateDTO;
import com.sky.takeout.dto.CategoryPageQueryDTO;
import com.sky.takeout.dto.CategoryUpdateDTO;
import com.sky.takeout.service.CategoryService;
import com.sky.takeout.vo.CategoryVO;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/page")
    public Result<PageResult<CategoryVO>> page(@Valid CategoryPageQueryDTO query) {
        return Result.success(categoryService.pageQuery(query));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        return Result.success(categoryService.create(categoryCreateDTO));
    }

    @GetMapping("/{id}")
    public Result<CategoryVO> getById(@PathVariable @Positive Long id) {
        return Result.success(categoryService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<CategoryVO>> listByType(
            @RequestParam @Min(1) @Max(2) Integer type
    ) {
        return Result.success(categoryService.listByType(type));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.update(categoryUpdateDTO);
        return Result.success(null);
    }

    @PostMapping("/status/{status}")
    public Result<Void> updateStatus(
            @PathVariable @Min(0) @Max(1) Integer status,
            @RequestParam @Positive Long id
    ) {
        categoryService.updateStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Long id) {
        categoryService.delete(id);
        return Result.success(null);
    }
}
