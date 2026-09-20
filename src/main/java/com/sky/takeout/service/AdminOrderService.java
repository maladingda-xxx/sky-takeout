package com.sky.takeout.service;

import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.dto.OrderAdminCancelDTO;
import com.sky.takeout.dto.OrderConfirmDTO;
import com.sky.takeout.dto.OrderRejectionDTO;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;

public interface AdminOrderService {

    PageResult<OrderVO> pageQuery(AdminOrderPageQueryDTO query);

    OrderVO getById(Long id);

    OrderStatisticsVO statistics();

    void confirm(OrderConfirmDTO confirmDTO);

    void reject(OrderRejectionDTO rejectionDTO);

    void cancel(OrderAdminCancelDTO cancelDTO);

    void delivery(Long id);

    void complete(Long id);
}
