package com.stylefeng.guns.rest.common.persistence.model;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 订单去重表
 * </p>
 *
 * @author gongyu
 * @since 2019-06-16
 */
@TableName("remove_duplicate")
public class RemoveDuplicate extends Model<RemoveDuplicate> {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId("UUID")
    private String uuid;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    protected Serializable pkVal() {
        return this.uuid;
    }

    @Override
    public String toString() {
        return "RemoveDuplicate{" +
        "uuid=" + uuid +
        "}";
    }
}
