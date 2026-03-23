package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.controller.admin.ShopController;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    public AddressBookMapper addressBookMapper;
    @Autowired
    public OrderMapper orderMapper;
    @Autowired
    public OrderDetailMapper orderDetailMapper;
    @Autowired
    public ShoppingCartMapper shoppingCartMapper;
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1. 检查用户地址、购物车是否为空 -> 异常
        Long userId = BaseContext.getCurrentId();

        //购物车
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if(shoppingCarts == null || shoppingCarts.isEmpty()) throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        //地址
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null) throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);

        //检查店铺营业状态
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(ShopController.KEY);
        if (shopStatus == null || shopStatus == 0) {
            throw new OrderBusinessException(MessageConstant.SHOP_CLOSED);
        }

        //2. 添加1条订单
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);
        //3. 添加n条订单详细
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        for(ShoppingCart sc: shoppingCarts){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        //4. 清空购物车
        shoppingCartMapper.deleteById(userId);
        //5. 返回VO数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId()).orderNumber(orders.getNumber()).orderAmount(orders.getAmount()).orderTime(orders.getOrderTime()).build();
        return orderSubmitVO;
    }

    /**
     * 支付订单
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
        //直接调用成功支付函数
        paySuccess(ordersPaymentDTO.getOrderNumber());
        //5.返回VO对象
        return new OrderPaymentVO();
    }

    /**
     * 用户查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery4user(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOS = new ArrayList<>();
        if (page != null || !page.isEmpty()) {
            for (Orders orders : page) {
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOS);

    }

    /**
     * 查询订单详细信息
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        //获取订单状态
        Orders orders = orderMapper.getById(id);
        /*
          订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
         */
        Integer status = orders.getStatus();
//        - 商家已接单状态下，用户取消订单需电话沟通商家
//        - 派送中状态下，用户取消订单需电话沟通商家
        if(status != Orders.PENDING_PAYMENT && status != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
//        - 待支付和待接单状态下，用户可直接取消订单
//        - 如果在待接单状态下取消订单，需要给用户退款
//        - 取消订单后需要将订单状态修改为“已取消”
        Orders cancelOrders = Orders.builder()
                        .id(id)
                        .status(Orders.CANCELLED)
                        .cancelReason(MessageConstant.USER_CANCEL)
                        .cancelTime(LocalDateTime.now())
                        .build();
        orderMapper.update(cancelOrders);
    }

    /**
     * 用户再来一单
     * @param id
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        //将菜品添加到购物车中
        //菜品只存在于OrderDetail中
        //获取菜品信息
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        //获取用户id
        Long userId = BaseContext.getCurrentId();
        //数据类型转换
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for(OrderDetail od : orderDetails){
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();
            BeanUtils.copyProperties(od,shoppingCart,"id");//第三个参数是忽略属性
            shoppingCarts.add(shoppingCart);
        }
        //将菜品批量加入购物车
        shoppingCartMapper.insertBatch(shoppingCarts);

    }

    /**
     * 管理员条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery4admin(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOS = new ArrayList<>();
        if (page != null || !page.isEmpty()) {
            for (Orders orders : page) {
//                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
//                orderVO.setOrderDetailList(orderDetails);
                String orderDishes = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOS);
    }

    @Override
    public OrderStatisticsVO statistics() {
        return orderMapper.statistics();
    }

    /**
     * 商家接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 商家拒单
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //获取订单信息
        Long id = ordersRejectionDTO.getId();
        Orders orders = orderMapper.getById(id);
//        - 只有订单处于“待接单”状态时可以执行拒单操作
        if(orders.getStatus() != Orders.TO_BE_CONFIRMED)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
//        - 商家拒单其实就是将订单状态修改为“已取消”
//        - 商家拒单时需要指定拒单原因
        Orders orders1 = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders1);
//        - 商家拒单时，如果用户已经完成了支付，需要为用户退款


    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        //获取订单信息
        Long id = ordersCancelDTO.getId();
//        - 取消订单其实就是将订单状态修改为“已取消”
//        - 商家取消订单时需要指定取消原因
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
//        - 商家取消订单时，如果用户已经完成了支付，需要为用户退款
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
//        - 只有状态为“待派送”的订单可以执行派送订单操作
        if(orderMapper.getById(id).getStatus() == Orders.CONFIRMED) {
//        - 派送订单其实就是将订单状态修改为“派送中”
            Orders orders = Orders.builder()
                    .id(id)
                    .status(Orders.DELIVERY_IN_PROGRESS)
                    .build();
            orderMapper.update(orders);
        }
        else throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
//        - 只有状态为“派送中”的订单可以执行订单完成操作
        if(orderMapper.getById(id).getStatus() == Orders.DELIVERY_IN_PROGRESS) {
//        - 完成订单其实就是将订单状态修改为“已完成”
            Orders orders = Orders.builder()
                    .id(id)
                    .status(Orders.COMPLETED)
                    .deliveryTime(LocalDateTime.now())
                    .build();
            orderMapper.update(orders);
        }
    }

    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 支付成功
     * @param orderNumber
     */
    public void paySuccess(String orderNumber){
        //1. 获取订单
        Orders orders = orderMapper.getByNumber(orderNumber);
        //2. 如果订单已支付，返回（防止二次支付）
        //3. 修改订单支付状态
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders1 = Orders.builder()
                .id(orders.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        //4. 更新订单
        orderMapper.update(orders1);
    }

}
