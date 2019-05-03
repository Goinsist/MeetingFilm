package com.stylefeng.guns.rest.common.persistence.dao;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrderT;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单信息表 Mapper 接口
 * </p>
 *
 * @author gongyu
 * @since 2019-04-16
 */
public interface MoocOrderTMapper extends BaseMapper<MoocOrderT> {
String getSeatsByFieldId(@Param("fieldId") String fieldId);
OrderVO getOrderInfoById(@Param("orderId") String orderId);
List<OrderVO> getOrdersByUserId(@Param("userId") Integer userId);
String getSoldSeatsByFieldId(@Param("field_id") Integer field_id);
}
