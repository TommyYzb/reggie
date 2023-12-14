package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.entity.Orders;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     */

    public void submit(Orders orders);
}
