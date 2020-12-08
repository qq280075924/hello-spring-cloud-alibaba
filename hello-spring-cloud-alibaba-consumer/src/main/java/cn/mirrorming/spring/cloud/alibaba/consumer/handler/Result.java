package cn.mirrorming.spring.cloud.alibaba.consumer.handler;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * Http请求返回的最外层对象
 * @author Administrator
 *
 */
@Getter
@Setter
@ToString
public class Result<T> {
	
	private ResultCodeEnum codeEnum;	//异常码
	
	private int code;	//异常码
	private String msg;	//异常信息
	
	private boolean success;
	
	private T data;	//返回到前端的数据
	
	public Result() {
    }
	public Result(ResultCodeEnum resultCodeEnum) {
        this.codeEnum = resultCodeEnum;
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMsg();
    }

    public Result(ResultCodeEnum resultCodeEnum, String message) {
        this.codeEnum = resultCodeEnum;
        this.code = resultCodeEnum.getCode();
        if(null !=message) {
        	this.msg = message;
        }else {
        	this.msg = resultCodeEnum.getMsg();
        }
    }
}
