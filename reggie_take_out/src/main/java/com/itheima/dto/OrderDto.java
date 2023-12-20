package com.itheima.dto;

import com.itheima.entity.OrderDetail;
import com.itheima.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {
    private List<OrderDetail> orderDetails;
}
