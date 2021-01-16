package com.heida.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.heida.constant.GlobalConstant;
import com.heida.dao.SeckillDao;
import com.heida.entity.PromotionSeckill;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class SeckillTask {

    @Resource
    private SeckillDao seckillDao;
    @Resource
    private RedisTemplate redisTemplate;


    Map<Integer,PromotionSeckill> allSeckills=new HashMap<>();



    @Scheduled(cron = "0/5 * * * * ?")
    public void test(){
        //每次新增的活动时，原标志位是0，将活动和活动对应的令牌塞进去以后将标志位设置为1
        //下次定时任务来的时候，当没有新数据进入数据库时，就不会更新redis中的数据
        List<PromotionSeckill> unstartSeckills = seckillDao.findUnstartSeckill();
        for (PromotionSeckill unstartSeckill : unstartSeckills) {
            //活动塞进redis在过滤器时用
            allSeckills.put(unstartSeckill.getPsId(),unstartSeckill);
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
            //活动塞进redis在过滤器时用
            //再来新的则删除旧的更新新的
            redisTemplate.delete("allSeckills");
            redisTemplate.opsForHash().putAll("allSeckills",allSeckills);
        }

    }




    @Scheduled(cron = "0/5 * * * * ?")
    public void endSeckill() throws ParseException {
        //现在redis中的活动都是标志位是1的
        Map<Integer, PromotionSeckill> allSeckills =(Map<Integer,PromotionSeckill>) redisTemplate.opsForHash().entries("allSeckills");
        Iterator<Integer> iterator = allSeckills.keySet().iterator();
        while (iterator.hasNext()){
            Integer key = iterator.next();
            PromotionSeckill promotionSeckill = allSeckills.get(key);
            //秒杀活动结束时间
            Date endTime = promotionSeckill.getEndTime();
            //当前时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            String currentTime = df.format(new Date());
            Date current = df.parse(currentTime);
            if (current.compareTo(endTime)>0){
                iterator.remove();
                redisTemplate.delete("allSeckills");
                redisTemplate.opsForHash().putAll("allSeckills",allSeckills);
            }else {
                continue;
            }
        }


        /*
        List<PromotionSeckill> expireSeckills = seckillDao.findExpireSeckill();
        for (PromotionSeckill expireSeckill : expireSeckills) {
            System.out.println("活动已经结束了"+expireSeckill.getPsId());
            expireSeckill.setStatus(GlobalConstant.OperationConstant.SECKILL_END);
            seckillDao.update(expireSeckill);
            redisTemplate.delete("seckill:count:"+expireSeckill.getPsId());
        }
        */
    }






    /*
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

    */
}
