package com.heida.scheduler;

import com.heida.constant.GlobalConstant;
import com.heida.dao.SeckillDao;
import com.heida.entity.PromotionSeckill;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SeckillTask {
    @Resource
    private SeckillDao seckillDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/5 * * * * ?")
    public void startSeckill(){
        List<PromotionSeckill> unstartSeckills = seckillDao.findUnstartSeckill();
        for (PromotionSeckill unstartSeckill : unstartSeckills) {
            System.out.println("本次秒杀活动开始了"+unstartSeckill.getPsId());
            //删除之前重复的活动
            redisTemplate.delete("seckill:count:"+unstartSeckill.getPsId());
            //本次秒杀多少件商品就在redis中存几个令牌
            for (int i = 0; i < unstartSeckill.getGoodsCount(); i++) {
                redisTemplate.opsForList().rightPush("seckill:count:"+unstartSeckill.getPsId(),unstartSeckill.getGoodsId());
            }
            //把活动开启的标志位打开
            unstartSeckill.setStatus(GlobalConstant.OperationConstant.SECKILL_START);
            seckillDao.update(unstartSeckill);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void endSeckill(){
        List<PromotionSeckill> expireSeckills = seckillDao.findExpireSeckill();
        for (PromotionSeckill expireSeckill : expireSeckills) {
            System.out.println("活动已经结束了"+expireSeckill.getPsId());
            expireSeckill.setStatus(GlobalConstant.OperationConstant.SECKILL_END);
            seckillDao.update(expireSeckill);
            redisTemplate.delete("seckill:count:"+expireSeckill.getPsId());
        }
    }
}
