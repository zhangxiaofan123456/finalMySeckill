package com.heida.service;

import com.heida.Exception.SeckillException;

public interface SeckillService {

    public void startSeckill(Integer psId, Integer userId) throws SeckillException;
    public String sendOrderToQueue(Integer userId);

}
