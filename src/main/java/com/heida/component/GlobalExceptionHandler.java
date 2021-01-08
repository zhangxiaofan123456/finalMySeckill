package com.heida.component;

import com.heida.Exception.SeckillException;
import com.heida.entity.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SeckillException.class)
    public Response bizException(SeckillException e){
        return new Response(500,e.getMessage(),null);
    }

    @ExceptionHandler(Exception.class)
    public Response exceptionHandler(Exception e){
        e.printStackTrace();
        return new Response(500,"系统开了小差",null);
    }

}
