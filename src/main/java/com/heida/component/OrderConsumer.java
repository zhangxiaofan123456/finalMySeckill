package com.heida.component;

import com.alibaba.fastjson.JSONObject;
import com.heida.constant.GlobalConstant;
import com.heida.dao.OrderDao;
import com.heida.dao.SeckillDao;
import com.heida.entity.Order;
import com.heida.entity.PromotionSeckill;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderConsumer {
    @Resource
    private OrderDao orderDAO;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SeckillDao seckillDao;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "queue-order") ,
                    exchange = @Exchange(value = "exchange-order" , type = "fanout")
            )
    )
    @RabbitHandler
    @Transactional(rollbackFor = Throwable.class)
    public void handleMessage(@Payload Map data , Channel channel ,
                              @Headers Map<String,Object> headers) throws IOException {
        //这里使用事物保证减库存和生成订单一起提交或者失败回滚
        //但是要注意一旦使用@Transactional你就别try catch了 直接throw
        //(rollbackFor = Throwable.class)，抛出一个最大的异常,Throwable.class是Exception的父类
        System.out.println("=======获取到订单数据:" + data + "===========);");
        //在consumer端防止重复消费
        String uuId = UUID.randomUUID().toString();
        String s = JSONObject.toJSONString(data);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(s, uuId);
        if (success){
            PromotionSeckill seckill = seckillDao.findByPsId(1);
            seckill.setGoodsCount(seckill.getGoodsCount()-1);
            seckillDao.update(seckill);
            //在数据库中通过联合索引来保证同一个用户同一个秒杀活动只能生成一个订单
            //让user_id和goods_id做一个联合主键
            Order order = new Order();
            order.setOrderNo(data.get("orderNo").toString());
            order.setOrderStatus(0);
            order.setUserId((Integer) data.get("userId"));
            order.setRecvName("xxx");
            order.setRecvMobile("1393310xxxx");
            order.setRecvAddress("xxxxxxxxxx");
            order.setAmount(19.8f);
            order.setPostage(0f);
            order.setCreateTime(new Date());
            orderDAO.insert(order);
            Long tag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
            //消息确认
            channel.basicAck(tag , false);
            System.out.println(data.get("orderNo") + "订单已创建");
        }else{
            //如果订单已经创建了就不去创建订单了
            Long tag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
            channel.basicAck(tag , false);
        }

    }
}
