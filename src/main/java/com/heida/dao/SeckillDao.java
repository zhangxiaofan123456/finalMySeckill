package com.heida.dao;

import com.heida.entity.PromotionSeckill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface SeckillDao {
    public List<PromotionSeckill> findUnstartSeckill();
    public void update(PromotionSeckill promotionSeckill);
    public List<PromotionSeckill> findExpireSeckill();
    public   PromotionSeckill  findByPsId(@Param("value") Integer psId);
}
