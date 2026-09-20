package com.sky.takeout.service.impl;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.OrderCancelDTO;
import com.sky.takeout.dto.OrderPageQueryDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.entity.AddressBook;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.Order;
import com.sky.takeout.entity.OrderDetail;
import com.sky.takeout.entity.Setmeal;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.AddressBookMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.OrderMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.mapper.ShoppingCartMapper;
import com.sky.takeout.service.OrderService;
import com.sky.takeout.vo.OrderItemVO;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import com.sky.takeout.vo.ShoppingCartVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int PENDING_PAYMENT = 1;
    private static final int TO_BE_ACCEPTED = 2;
    private static final int UNPAID = 0;
    private static final int ENABLED = 1;
    private static final int MAX_PAGE_SIZE = 100;

    private final OrderMapper orderMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public OrderServiceImpl(
            OrderMapper orderMapper,
            ShoppingCartMapper shoppingCartMapper,
            AddressBookMapper addressBookMapper,
            DishMapper dishMapper,
            SetmealMapper setmealMapper
    ) {
        this.orderMapper = orderMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.addressBookMapper = addressBookMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    @Transactional
    public OrderSubmitVO submit(OrderSubmitDTO submitDTO) {
        Long userId = UserContext.getRequiredUserId();
        AddressBook address = resolveAddress(userId, submitDTO.getAddressBookId());
        List<ShoppingCartVO> cartItems = shoppingCartMapper.selectByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Shopping cart is empty"
            );
        }

        List<OrderDetail> details = new ArrayList<>(cartItems.size());
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCartVO cartItem : cartItems) {
            OrderDetail detail = buildOrderDetail(cartItem);
            details.add(detail);
            totalAmount = totalAmount.add(detail.getAmount());
        }

        LocalDateTime orderTime = LocalDateTime.now();
        Order order = new Order();
        order.setNumber(generateOrderNumber());
        order.setStatus(PENDING_PAYMENT);
        order.setUserId(userId);
        order.setAddressBookId(address.getId());
        order.setOrderTime(orderTime);
        order.setPayMethod(submitDTO.getPayMethod());
        order.setPayStatus(UNPAID);
        order.setAmount(totalAmount);
        order.setRemark(normalizeOptionalText(submitDTO.getRemark()));
        order.setPhone(address.getPhone());
        order.setAddress(formatAddress(address));
        order.setConsignee(address.getConsignee());

        insertOrderWithRetry(order);
        details.forEach(detail -> detail.setOrderId(order.getId()));
        if (orderMapper.insertDetails(details) != details.size()) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create order details"
            );
        }

        shoppingCartMapper.deleteByUserId(userId);
        return new OrderSubmitVO(
                order.getId(),
                order.getNumber(),
                order.getAmount(),
                order.getOrderTime()
        );
    }

    @Override
    @Transactional
    public void pay(OrderPaymentDTO paymentDTO) {
        Long userId = UserContext.getRequiredUserId();
        Order order = orderMapper.selectByNumberAndUserId(
                paymentDTO.getOrderNumber(),
                userId
        );
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Order not found");
        }

        if (order.getStatus() != PENDING_PAYMENT || order.getPayStatus() != UNPAID) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Order cannot be paid"
            );
        }

        if (orderMapper.markPaid(order.getId(), userId) != 1) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Order cannot be paid"
            );
        }
    }

    @Override
    public PageResult<OrderVO> pageQuery(OrderPageQueryDTO query) {
        normalizeQuery(query);
        Long userId = UserContext.getRequiredUserId();

        long total = orderMapper.countByQuery(userId, query);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }

        List<OrderVO> orders = orderMapper.selectPage(userId, query);
        fillDetails(orders);
        return new PageResult<>(total, orders);
    }

    @Override
    public OrderVO getById(Long id) {
        Long userId = UserContext.getRequiredUserId();
        Order order = orderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Order not found");
        }

        OrderVO orderVO = toVO(order);
        orderVO.setOrderDetailList(toItemVOs(
                orderMapper.selectDetailsByOrderIds(List.of(id))
        ));
        return orderVO;
    }

    @Override
    @Transactional
    public void cancel(Long id, OrderCancelDTO cancelDTO) {
        Long userId = UserContext.getRequiredUserId();
        Order order = orderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Order not found");
        }

        if (order.getStatus() != PENDING_PAYMENT
                && order.getStatus() != TO_BE_ACCEPTED) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Order cannot be cancelled"
            );
        }

        if (orderMapper.cancel(
                id,
                userId,
                normalizeOptionalText(cancelDTO.getReason())
        ) != 1) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to cancel order"
            );
        }
    }

    private AddressBook resolveAddress(Long userId, Long addressBookId) {
        AddressBook address = addressBookId == null
                ? addressBookMapper.selectDefaultByUserId(userId)
                : addressBookMapper.selectByIdAndUserId(addressBookId, userId);

        if (address == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Delivery address not found"
            );
        }
        return address;
    }

    private OrderDetail buildOrderDetail(ShoppingCartVO cartItem) {
        String name;
        String image;
        BigDecimal unitPrice;

        if ("dish".equals(cartItem.getProductType())) {
            Dish dish = dishMapper.selectById(cartItem.getProductId());
            if (dish == null || dish.getStatus() != ENABLED) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dish is not available"
                );
            }
            name = dish.getName();
            image = dish.getImage();
            unitPrice = dish.getPrice();
        } else {
            Setmeal setmeal = setmealMapper.selectById(cartItem.getProductId());
            if (setmeal == null || setmeal.getStatus() != ENABLED) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Setmeal is not available"
                );
            }
            name = setmeal.getName();
            image = setmeal.getImage();
            unitPrice = setmeal.getPrice();
        }

        OrderDetail detail = new OrderDetail();
        detail.setName(name);
        detail.setImage(image);
        detail.setDishId("dish".equals(cartItem.getProductType())
                ? cartItem.getProductId()
                : null);
        detail.setSetmealId("setmeal".equals(cartItem.getProductType())
                ? cartItem.getProductId()
                : null);
        detail.setDishFlavor(cartItem.getFlavor());
        detail.setNumber(cartItem.getQuantity());
        detail.setAmount(
                unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
        return detail;
    }

    private void insertOrderWithRetry(Order order) {
        for (int attempt = 0; attempt < 3; attempt++) {
            order.setNumber(generateOrderNumber());
            try {
                if (orderMapper.insert(order) == 1) {
                    return;
                }
            } catch (DuplicateKeyException ignored) {
                // Extremely unlikely; generate a new number and retry.
            }
        }

        throw new BusinessException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to create order"
        );
    }

    private void fillDetails(List<OrderVO> orders) {
        if (orders.isEmpty()) {
            return;
        }

        Map<Long, List<OrderDetail>> detailsByOrderId = orderMapper
                .selectDetailsByOrderIds(
                        orders.stream().map(OrderVO::getId).toList()
                )
                .stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        orders.forEach(order -> order.setOrderDetailList(
                toItemVOs(detailsByOrderId.getOrDefault(order.getId(), List.of()))
        ));
    }

    private List<OrderItemVO> toItemVOs(List<OrderDetail> details) {
        return details.stream()
                .map(detail -> new OrderItemVO(
                        detail.getName(),
                        detail.getImage(),
                        detail.getDishId(),
                        detail.getSetmealId(),
                        detail.getDishFlavor(),
                        detail.getNumber(),
                        detail.getAmount()
                ))
                .toList();
    }

    private OrderVO toVO(Order order) {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(order.getId());
        orderVO.setNumber(order.getNumber());
        orderVO.setStatus(order.getStatus());
        orderVO.setPayStatus(order.getPayStatus());
        orderVO.setPayMethod(order.getPayMethod());
        orderVO.setAmount(order.getAmount());
        orderVO.setRemark(order.getRemark());
        orderVO.setPhone(order.getPhone());
        orderVO.setAddress(order.getAddress());
        orderVO.setConsignee(order.getConsignee());
        orderVO.setOrderTime(order.getOrderTime());
        orderVO.setCheckoutTime(order.getCheckoutTime());
        orderVO.setCancelReason(order.getCancelReason());
        orderVO.setCancelTime(order.getCancelTime());
        return orderVO;
    }

    private void normalizeQuery(OrderPageQueryDTO query) {
        query.setPage(Math.max(query.getPage(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));
    }

    private String formatAddress(AddressBook address) {
        return address.getProvinceName()
                + address.getCityName()
                + address.getDistrictName()
                + address.getDetail();
    }

    private String generateOrderNumber() {
        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        );
        int random = ThreadLocalRandom.current().nextInt(1_000_000);
        return time + String.format("%06d", random);
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
