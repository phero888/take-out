package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;


@Mapper
public interface OrderMapper {
    /**
     * 添加订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 通过订单号获取订单
     * @param orderNumber
     * @return
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 更新订单
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 通过id获取订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select " +
            "sum(case when status = 2 then 1 else 0 end) as toBeConfirmed," +
            "sum(case when status = 3 then 1 else 0 end) as confirmed," +
            "sum(case when status = 4 then 1 else 0 end) as deliveryInProgress " +
            "from orders")
    OrderStatisticsVO statistics();

    @Select("select ifnull(sum(amount),0) from orders where order_time >= #{begin} and order_time <= #{end} and status != 5")
    Double sumAmountByDate(LocalDateTime begin, LocalDateTime end);

    @Select("select count(*) from orders where order_time >= #{begin} and order_time <= #{end} and status != 5")
    Integer countByDate(LocalDateTime begin, LocalDateTime end);

    @Select("select count(*) from orders where order_time >= #{begin} and order_time <= #{end}")
    Integer countAll();

    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);
}
