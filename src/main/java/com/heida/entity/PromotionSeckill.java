package com.heida.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionSeckill {
    private Integer psId;
    private Integer goodsId;
    private Integer goodsCount;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Float currentPrice;
}
