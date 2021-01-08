package com.heida.service.impl;

import com.heida.Exception.SeckillException;
import com.heida.constant.GlobalConstant;
import com.heida.dao.SeckillDao;
import com.heida.entity.PromotionSeckill;
import com.heida.service.SeckillService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
            //判断用户是否重复秒杀了
            Boolean isRepeat = redisTemplate.opsForSet().isMember("seckill:users:" + seckill.getPsId(), userId);
            if (!isRepeat){
                System.out.println("抢到商品了");
                redisTemplate.opsForSet().add("seckill:users:"+seckill.getPsId(),userId);
            }else {
                //如果重复了把刚才弹出去的加上
                redisTemplate.opsForList().rightPush("seckill:count:" + seckill.getPsId(),seckill.getGoodsId());
                throw new SeckillException("您已经参加过此活动");
            }
        }else {
            throw new SeckillException("商品已经被抢光了");
        }

    }

    @Override
    public String sendOrderToQueue(Integer userId) {
        System.out.println("向队列中发送消息");
        Map<String,Object> data=new HashMap();
        String orderNo = UUID.randomUUID().toString();
        data.put("userId",userId);
        data.put("orderNo",orderNo);
        //把data发给交换机
        rabbitTemplate.convertAndSend("exchange-order",null,data);
        return orderNo;
    }
}
