package com.heida.service.impl;

import com.heida.Exception.SeckillException;
import com.heida.constant.GlobalConstant;
import com.heida.dao.SeckillDao;
import com.heida.entity.PromotionSeckill;
import com.heida.service.SeckillService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private SeckillDao seckillDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Override
    public void startSeckill(Integer psId, Integer userId) throws SeckillException {
            //查询当前的秒杀活动是否存在
        PromotionSeckill seckill = seckillDao.findByPsId(psId);
        if (seckill==null){
            throw new SeckillException("秒杀活动不存在");
        }else if (seckill.getStatus()== GlobalConstant.OperationConstant.SECKILL_UNSTART){
            throw new SeckillException("秒杀活动还没有开始");
        }else if (seckill.getStatus()==GlobalConstant.OperationConstant.SECKILL_END){
            throw new SeckillException("秒杀活动已经结束");
        }
        //秒杀一次令牌从队列中弹出一次
        Integer goodsId =(Integer) redisTemplate.opsForList().leftPop("seckill:count:" + seckill.getPsId());
        //弹出成功就给用户存一个令牌
        if (goodsId!=null){
            redisTemplate.opsForSet().add("seckill:users:"+seckill.getPsId(),userId);
        }else {
            throw new SeckillException("商品已经被抢光了");
        }

    }

    @Override
    @Scope("prototype")
    public String sendOrderToQueue(Integer userId) {

        RabbitTemplate.ConfirmCallback confirmCallback= new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                //消息被mq拒收或者接收都会通过这个方法
                //correlationData消息的id
                //b为true代表消息被接收
                //s是消息被拒绝的原因
                System.out.println("000"+correlationData);
                System.out.println(b);
                System.out.println(s);
            }
        };
        RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                //参数依次为被退回的消息、错误编码、错误描述、交换机的名字、路由键
                System.out.println(message);
                System.out.println(i);
                System.out.println(s);
                System.out.println(s1);
                System.out.println(s2);
            }
        };


        System.out.println("向队列中发送消息");
        Map<String,Object> data=new HashMap();
        String orderNo = UUID.randomUUID().toString();
        data.put("userId",userId);
        data.put("orderNo",orderNo);
        //这个方法无论成功失败都会进入
        //rabbitTemplate.setConfirmCallback(confirmCallback);
        //只有失败才进入
        //rabbitTemplate.setReturnCallback(returnCallback);
        //把data发给交换机
        rabbitTemplate.convertAndSend("exchange-order",null,data);
        System.out.println("111"+data);
        return orderNo;
    }
}
