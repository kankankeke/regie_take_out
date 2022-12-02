package com.kankankeke.reggie.common;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){//将错误提示信息传进来
        super(message);
    }
}
