package com.sky.takeout.mapper;

import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.dto.OrderPageQueryDTO;
import com.sky.takeout.entity.Order;
import com.sky.takeout.entity.OrderDetail;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    int insert(Order order);

    Order selectByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    Order selectById(@Param("id") Long id);

    Order selectByNumberAndUserId(
            @Param("number") String number,
            @Param("userId") Long userId
    );

    long countByQuery(
            @Param("userId") Long userId,
            @Param("query") OrderPageQueryDTO query
    );

    List<OrderVO> selectPage(
            @Param("userId") Long userId,
            @Param("query") OrderPageQueryDTO query
    );

    long countByAdminQuery(
            @Param("query") AdminOrderPageQueryDTO query
    );

    List<OrderVO> selectAdminPage(
            @Param("query") AdminOrderPageQueryDTO query
    );

    OrderStatisticsVO selectStatistics();

    int markPaid(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    int cancel(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("reason") String reason
    );

    int transitionStatus(
            @Param("id") Long id,
            @Param("expectedStatus") Integer expectedStatus,
            @Param("targetStatus") Integer targetStatus
    );

    int reject(
            @Param("id") Long id,
            @Param("reason") String reason
    );

    int adminCancel(
            @Param("id") Long id,
            @Param("reason") String reason
    );

    int insertDetails(@Param("details") List<OrderDetail> details);

    List<OrderDetail> selectDetailsByOrderIds(
            @Param("orderIds") List<Long> orderIds
    );
}
