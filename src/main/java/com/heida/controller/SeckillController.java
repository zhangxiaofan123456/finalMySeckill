package com.heida.controller;

import com.heida.Exception.SeckillException;
import com.heida.dao.SeckillDao;
import com.heida.entity.PromotionSeckill;
import com.heida.entity.Response;
import com.heida.service.SeckillService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @Resource
    private SeckillDao seckillDao;

    @RequestMapping("/start")
    public Response processSeckill(Integer psId, Integer userId) throws SeckillException {
        //这个方法核心思想就是，该用户在当前秒杀成功就在list集合中弹出一个，并且在set集合中增加一个
        seckillService.startSeckill(psId,userId);
        //将用户信息发送给mq，直接返回对应的订单号，不同步等待消费者。当然上面如果秒杀失败了也不会给mq发消息了
        String orderNo = seckillService.sendOrderToQueue(userId);
        return new Response(200,"流程结束",orderNo);
    }




}
