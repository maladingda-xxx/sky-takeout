package com.sky.takeout.controller.user;

import com.sky.takeout.common.Result;
import com.sky.takeout.service.UserCatalogService;
import com.sky.takeout.vo.CategoryVO;
import com.sky.takeout.vo.DishUserVO;
import com.sky.takeout.vo.SetmealDetailVO;
import com.sky.takeout.vo.SetmealPageVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/user")
public class UserCatalogController {

    private final UserCatalogService userCatalogService;

    public UserCatalogController(UserCatalogService userCatalogService) {
        this.userCatalogService = userCatalogService;
    }

    @GetMapping("/category/list")
    public Result<List<CategoryVO>> listCategories(
            @RequestParam @Min(1) @Max(2) Integer type
    ) {
        return Result.success(userCatalogService.listCategories(type));
    }

    @GetMapping("/dish/list")
    public Result<List<DishUserVO>> listDishes(
            @RequestParam @Positive Long categoryId
    ) {
        return Result.success(userCatalogService.listDishes(categoryId));
    }

    @GetMapping("/setmeal/list")
    public Result<List<SetmealPageVO>> listSetmeals(
            @RequestParam @Positive Long categoryId
    ) {
        return Result.success(userCatalogService.listSetmeals(categoryId));
    }

    @GetMapping("/setmeal/{id}")
    public Result<SetmealDetailVO> getSetmealDetail(
            @PathVariable @Positive Long id
    ) {
        return Result.success(userCatalogService.getSetmealDetail(id));
    }
}
