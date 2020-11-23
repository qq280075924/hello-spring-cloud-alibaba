package cn.mirrorming.spring.cloud.alibaba.consumer.exception;

public class GlobalException extends RuntimeException{

    public GlobalException(String msg) {
        super(msg);
    }
}
