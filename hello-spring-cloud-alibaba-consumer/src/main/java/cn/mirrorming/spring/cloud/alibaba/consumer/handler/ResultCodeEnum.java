package cn.mirrorming.spring.cloud.alibaba.consumer.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author CGF
 * @date 2018/7/17 21:15
 * @Description 李健
 */
@AllArgsConstructor
public enum ResultCodeEnum {

	INSERTSUCCESS(1,"添加成功"),
    INSERTFAIL(-1,"添加失败"),
    UPDATESUCCESS(1,"修改成功"),
    UPDATEFAIL(-1,"修改失败"),
    DELETESUCCESS(1,"删除成功"),
    DELEFTEFAIL(-1,"删除失败"),
    SELECTSUCCESS(1,"查找成功"),
    SELECTFAIL(-1,"查找失败"),
    AUTHSUCCESS(1,"认证成功"),
    UNTHSUCCESS(-1,"认证失败"),

    SUCCESS(200,"成功"),//通过
    FAIL(400,"失败"), //失败
    NOTFOUND(404,"请求资源不存在"), //没有找到资源
    UNAUTHORIZEd(401,"未认证"),//未认证，签名错误
    INTERNAL_SERVER_ERROR(500,"服务器内部错误"), //服务器错误
    ;

	@Getter
    @Setter
    private int code;

	@Getter
    @Setter
    private String msg;
}
