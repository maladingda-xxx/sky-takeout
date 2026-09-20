package com.sky.takeout.controller.user;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.dto.ShoppingCartUpdateDTO;
import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.ShoppingCartVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ShoppingCartItemDTO itemDTO) {
        shoppingCartService.add(itemDTO);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<ShoppingCartVO>> list() {
        return Result.success(shoppingCartService.list());
    }

    @PostMapping("/sub")
    public Result<Void> sub(@Valid @RequestBody ShoppingCartItemDTO itemDTO) {
        shoppingCartService.sub(itemDTO);
        return Result.success(null);
    }

    @PostMapping("/update")
    public Result<Void> update(
            @Valid @RequestBody ShoppingCartUpdateDTO updateDTO
    ) {
        shoppingCartService.updateQuantity(updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/clean")
    public Result<Void> clean() {
        shoppingCartService.clean();
        return Result.success(null);
    }
}
