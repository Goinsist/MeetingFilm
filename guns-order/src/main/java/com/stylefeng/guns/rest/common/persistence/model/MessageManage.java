package com.stylefeng.guns.rest.common.persistence.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 异常过时未支付订单消费信息表
 * </p>
 *
 * @author gongyu
 * @since 2019-06-25
 */
@TableName("message_manage")
public class MessageManage extends Model<MessageManage> {

    private static final long serialVersionUID = 1L;

    /**
     * 消息全局唯一id
     */
    private Integer id;
    /**
     * 订单全局唯一id
     */
    @TableField("order_id")
    private Integer orderId;
    /**
     * 0-消息未消费,1-消息已消费
     */
    private Integer stauts;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
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
