package com.stylefeng.guns.rest.common.persistence.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;

/**
 * <p>
 * 异常过时未支付订单信息表
 * </p>
 *
 * @author gongyu
 * @since 2019-06-25
 */
@TableName("message_exception")
public class MessageException extends Model<MessageException> {

    private static final long serialVersionUID = 1L;

    /**
     * 消息全局唯一ID
     */
    private Integer id;
    /**
     * 异常订单ID
     */
    @TableField("order_id")
    private String orderId;
    /**
     * 0-未处理,1-已处理
     */
    private Integer status;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "MessageException{" +
        "id=" + id +
        ", orderId=" + orderId +
        ", status=" + status +
        "}";
    }
}
