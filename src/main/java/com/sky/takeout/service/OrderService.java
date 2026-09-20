package com.sky.takeout.service;

import com.sky.takeout.dto.OrderCancelDTO;
import com.sky.takeout.dto.OrderPageQueryDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;

public interface OrderService {

    OrderSubmitVO submit(OrderSubmitDTO submitDTO);

    void pay(OrderPaymentDTO paymentDTO);

    PageResult<OrderVO> pageQuery(OrderPageQueryDTO query);

    OrderVO getById(Long id);

    void cancel(Long id, OrderCancelDTO cancelDTO);
}
