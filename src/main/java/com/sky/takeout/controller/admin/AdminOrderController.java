package com.sky.takeout.controller.admin;

import com.sky.takeout.common.Result;
import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.dto.OrderAdminCancelDTO;
import com.sky.takeout.dto.OrderConfirmDTO;
import com.sky.takeout.dto.OrderRejectionDTO;
import com.sky.takeout.service.AdminOrderService;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping("/conditionSearch")
    public Result<PageResult<OrderVO>> conditionSearch(
            @Valid AdminOrderPageQueryDTO query
    ) {
        return Result.success(adminOrderService.pageQuery(query));
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        return Result.success(adminOrderService.statistics());
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable @Positive Long id) {
        return Result.success(adminOrderService.getById(id));
    }

    @PutMapping("/confirm")
    public Result<Void> confirm(
            @Valid @RequestBody OrderConfirmDTO confirmDTO
    ) {
        adminOrderService.confirm(confirmDTO);
        return Result.success(null);
    }

    @PutMapping("/rejection")
    public Result<Void> rejection(
            @Valid @RequestBody OrderRejectionDTO rejectionDTO
    ) {
        adminOrderService.reject(rejectionDTO);
        return Result.success(null);
    }

    @PutMapping("/cancel")
    public Result<Void> cancel(
            @Valid @RequestBody OrderAdminCancelDTO cancelDTO
    ) {
        adminOrderService.cancel(cancelDTO);
        return Result.success(null);
    }

    @PutMapping("/delivery/{id}")
    public Result<Void> delivery(@PathVariable @Positive Long id) {
        adminOrderService.delivery(id);
        return Result.success(null);
    }

    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable @Positive Long id) {
        adminOrderService.complete(id);
        return Result.success(null);
    }
}
