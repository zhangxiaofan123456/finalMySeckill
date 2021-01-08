package com.heida.component;

import com.heida.constant.GlobalConstant;
import com.heida.dao.OrderDao;
import com.heida.entity.Order;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Component
public class OrderConsumer {
    @Resource
    private OrderDao orderDAO;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "queue-order") ,
                    exchange = @Exchange(value = "exchange-order" , type = "fanout")
            )
    )
    @RabbitHandler
    public void handleMessage(@Payload Map data , Channel channel ,
                              @Headers Map<String,Object> headers){
        System.out.println("=======获取到订单数据:" + data + "===========);");

        try {
            //对接支付宝、对接物流系统、日志登记。。。。
            /*
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
