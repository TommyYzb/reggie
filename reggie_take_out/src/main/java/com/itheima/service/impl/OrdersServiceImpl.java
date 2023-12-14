package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.BaseContext;
import com.itheima.common.CustomException;
import com.itheima.entity.*;
import com.itheima.mapper.OrdersMapper;
import com.itheima.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional  // 事务控制
    public void submit(Orders orders) {
        // 获取当前用户id
        Long userId = BaseContext.getCurrentId();

        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        // 购物车为空判断
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单!");
        }

        // 查询用户数据
        User user = userService.getById(userId);

        // 查询地址簿数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        // 用户空地址判断
        if (addressBook == null ) {
            throw new CustomException("地址信息有误，不能下单!");
        }

        long orderId = IdWorker.getId(); // 订单号
        AtomicInteger amount = new AtomicInteger(0);  // 原子操作  保证线程安全

        // 利用stream流遍历
        List<OrderDetail> orderDetails =  shoppingCarts.stream().map((item)-> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);   // 订单号
            orderDetail.setNumber(item.getNumber());    // 订单数
            orderDetail.setDishFlavor(item.getDishFlavor());   // 菜品口味
            orderDetail.setDishId(item.getDishId());    // 菜品id
            orderDetail.setSetmealId(item.getSetmealId());    // 套餐Id
            orderDetail.setName(item.getName());       // 菜品名称  |   套餐名称   | 订单名称
            orderDetail.setImage(item.getImage());      // 订单图片
            orderDetail.setAmount(item.getAmount());    // 总金额数
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());  //
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);      // 订单id
        orders.setOrderTime(LocalDateTime.now());  // 下单时间
        orders.setCheckoutTime(LocalDateTime.now());   // 审核时间
        orders.setStatus(2);        // 设置订单状态（2：待派送）
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);   // 用户id
        orders.setNumber(String.valueOf(orderId));     // 订单数
        orders.setUserName(user.getName());     // 用户名称
        orders.setConsignee(addressBook.getConsignee());  // 收货人
        orders.setPhone(addressBook.getPhone());    // 用户联系方式
        // 订单地址
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        // 向订单表插入数据
        this.save(orders);

        // 向订单明细表插入数据
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
}
