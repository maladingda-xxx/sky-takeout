package com.sky.takeout.controller.user;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.OrderCancelDTO;
import com.sky.takeout.dto.OrderPageQueryDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.service.OrderService;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/user/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(
            @Valid @RequestBody OrderSubmitDTO submitDTO
    ) {
        return Result.success(orderService.submit(submitDTO));
    }

    @PutMapping("/payment")
    public Result<Void> payment(
            @Valid @RequestBody OrderPaymentDTO paymentDTO
    ) {
        orderService.pay(paymentDTO);
        return Result.success(null);
    }

    @GetMapping("/history")
    public Result<PageResult<OrderVO>> history(
            @Valid OrderPageQueryDTO query
    ) {
        return Result.success(orderService.pageQuery(query));
    }

    @GetMapping("/detail/{id}")
    public Result<OrderVO> detail(@PathVariable @Positive Long id) {
        return Result.success(orderService.getById(id));
    }

    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(
            @PathVariable @Positive Long id,
            @Valid @RequestBody OrderCancelDTO cancelDTO
    ) {
        orderService.cancel(id, cancelDTO);
        return Result.success(null);
    }
}
