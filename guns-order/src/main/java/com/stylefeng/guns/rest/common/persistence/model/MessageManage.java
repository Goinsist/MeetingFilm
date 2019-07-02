package com.stylefeng.guns.rest.common.persistence.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * <p>
 * 异常过时未支付订单消费信息表
 * </p>
 *
 * @author gongyu
 * @since 2019-06-26
 */
@TableName("message_manage")
public class MessageManage extends Model<MessageManage> {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.INPUT)
    private String id;
    @TableField("order_id")
    private String orderId;
    /**
     * 0-消息未消费,1-消息已消费
     */
    private Integer stauts;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getStauts() {
        return stauts;
    }

    public void setStauts(Integer stauts) {
        this.stauts = stauts;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "MessageManage{" +
        "id=" + id +
        ", orderId=" + orderId +
        ", stauts=" + stauts +
        "}";
    }
}
