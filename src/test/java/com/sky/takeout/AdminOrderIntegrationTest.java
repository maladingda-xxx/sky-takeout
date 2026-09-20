package com.sky.takeout;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.OrderAdminCancelDTO;
import com.sky.takeout.dto.OrderConfirmDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderRejectionDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.service.AdminOrderService;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.OrderService;
import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminOrderIntegrationTest {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private DishService dishService;

    @BeforeEach
    void setUpContext() {
        UserContext.setUserId(88L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldManageOrderLifecycleAndStatistics() {
        Long dishId = createDish();
        createAddress();

        OrderSubmitVO completedOrder = createPaidOrder(dishId, 2);
        OrderStatisticsVO beforeConfirm = adminOrderService.statistics();
        assertTrue(beforeConfirm.getToBeConfirmed() >= 1);

        AdminOrderPageQueryDTO query = new AdminOrderPageQueryDTO();
        query.setNumber(completedOrder.getOrderNumber());
        query.setWithDetails(true);
        PageResult<OrderVO> page = adminOrderService.pageQuery(query);
        assertEquals(1, page.getTotal());
        assertEquals(
                completedOrder.getOrderNumber(),
                page.getRecords().get(0).getNumber()
        );
        assertEquals(
                1,
                page.getRecords().get(0).getOrderDetailList().size()
        );

        OrderConfirmDTO confirmDTO = new OrderConfirmDTO();
        confirmDTO.setId(completedOrder.getId());
        adminOrderService.confirm(confirmDTO);
        assertEquals(3, adminOrderService.getById(completedOrder.getId()).getStatus());

        adminOrderService.delivery(completedOrder.getId());
        assertEquals(4, adminOrderService.getById(completedOrder.getId()).getStatus());

        adminOrderService.complete(completedOrder.getId());
        assertEquals(5, adminOrderService.getById(completedOrder.getId()).getStatus());

        OrderSubmitVO rejectedOrder = createPaidOrder(dishId, 1);
        OrderRejectionDTO rejectionDTO = new OrderRejectionDTO();
        rejectionDTO.setId(rejectedOrder.getId());
        rejectionDTO.setReason("Out of stock");
        adminOrderService.reject(rejectionDTO);

        OrderVO rejectedDetail = adminOrderService.getById(rejectedOrder.getId());
        assertEquals(6, rejectedDetail.getStatus());
        assertEquals(2, rejectedDetail.getPayStatus());
        assertEquals("Out of stock", rejectedDetail.getCancelReason());

        OrderSubmitVO cancelledOrder = createPaidOrder(dishId, 1);
        OrderAdminCancelDTO cancelDTO = new OrderAdminCancelDTO();
        cancelDTO.setId(cancelledOrder.getId());
        cancelDTO.setReason("Store closed");
        adminOrderService.cancel(cancelDTO);

        OrderVO cancelledDetail = adminOrderService.getById(cancelledOrder.getId());
        assertEquals(6, cancelledDetail.getStatus());
        assertEquals(2, cancelledDetail.getPayStatus());
        assertEquals("Store closed", cancelledDetail.getCancelReason());
    }

    private OrderSubmitVO createPaidOrder(Long dishId, int quantity) {
        ShoppingCartItemDTO itemDTO = new ShoppingCartItemDTO();
        itemDTO.setDishId(dishId);
        itemDTO.setQuantity(quantity);
        shoppingCartService.add(itemDTO);

        OrderSubmitDTO submitDTO = new OrderSubmitDTO();
        submitDTO.setPayMethod(1);
        OrderSubmitVO submitted = orderService.submit(submitDTO);

        OrderPaymentDTO paymentDTO = new OrderPaymentDTO();
        paymentDTO.setOrderNumber(submitted.getOrderNumber());
        orderService.pay(paymentDTO);
        return submitted;
    }

    private Long createDish() {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName("Admin Order Dish");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("12.50"));
        dto.setStatus(1);
        return dishService.create(dto);
    }

    private void createAddress() {
        AddressBookCreateDTO dto = new AddressBookCreateDTO();
        dto.setConsignee("Admin Order User");
        dto.setSex(1);
        dto.setPhone("13800000010");
        dto.setProvinceName("Shanghai");
        dto.setCityName("Shanghai");
        dto.setDistrictName("Pudong");
        dto.setDetail("Admin Order Road");
        dto.setIsDefault(1);
        addressBookService.create(dto);
    }
}
