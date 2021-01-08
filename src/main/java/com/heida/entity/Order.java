package com.heida.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Integer orderId;
    private String orderNo;
    private Integer orderStatus;
    private Integer userId;
    private String recvName;
    private String recvAddress;
    private String recvMobile;
    private Float postage;
    private Float amount;
    private Date createTime;
}
