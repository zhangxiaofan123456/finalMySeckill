package com.heida.dao;

import com.heida.entity.PromotionSeckill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface SeckillDao {
     List<PromotionSeckill> findUnstartSeckill();
     void update(PromotionSeckill promotionSeckill);
     List<PromotionSeckill> findExpireSeckill();
     PromotionSeckill  findByPsId(@Param("value") Integer psId);
}
