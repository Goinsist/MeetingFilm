package com.stylefeng.guns.rest.modular.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVO;
import com.stylefeng.guns.api.cinema.vo.OrderQueryVO;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.UUIDUtil;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrder2019TMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrder2019T;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import com.stylefeng.guns.rest.common.util.SnowFlake;
import com.stylefeng.guns.rest.modular.order.rabbitmq.RabbitSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Component
@Service(interfaceClass = OrderServiceAPI.class,group = "order2019",filter = "tracing")
public class OrderServiceImpl2019 implements OrderServiceAPI {
    @Autowired
    private MoocOrder2019TMapper moocOrder2019TMapper;
    @Reference(interfaceClass = CinemaServiceAPI.class,check = false,filter = "tracing")
    private CinemaServiceAPI cinemaServiceAPI;
    @Autowired
    private FTPUtil ftpUtil;
    @Autowired
    private RabbitSender rabbitSender;


    //验证是否为真实的座位编号
    @Override
    public boolean isTrueSeats(String fieldId, String seats) {
        //根据fieldId找到对应的座位位置图
        String seatPath = moocOrder2019TMapper.getSeatsByFieldId(fieldId);
        //读取位置图，判断seats是否为真
        String fileStrByAddress = ftpUtil.getFileStrByAddress(seatPath);
        //将影厅的json字符串转换为json对象
        JSONObject jsonObject= JSONObject.parseObject(fileStrByAddress);
        //seats=1,2,3 ids="1,3,4,5,6"
        String ids= jsonObject.get("ids").toString();
        //每一次匹配上的，都给isTrue+1
        String[] seatArrs=seats.split(",");
        String[] idArrs=ids.split(",");
        int  isTrue=0;
        for(String str:idArrs){
            for(String seat:seatArrs){
                if(seat.equalsIgnoreCase(str)){
                    isTrue++;
                }
            }
        }
        //如果匹配上的数量与已售座位数一致，则表示完全都匹配了
        if(seatArrs.length==isTrue){
            return true;
        }else {
            return false;
        }

    }
    //判断是否为已售座位
    @Override
    public boolean isNotSoldSeats(String fieldId, String seats) {
        EntityWrapper<MoocOrder2019T> entityWrapper=new EntityWrapper();
        entityWrapper.eq("field_id",fieldId);
        List<MoocOrder2019T> moocOrderTS = moocOrder2019TMapper.selectList(entityWrapper);
        String[] seatArrs=seats.split(",");
        String[] soldseats;
        List<String> soldList=new ArrayList<>();
        //有任何一个编号匹配上，则直接返回失败
        for(MoocOrder2019T moocOrderT:moocOrderTS){
            String solds=  moocOrderT.getSeatsIds();
            soldseats=solds.split(",");
            for(int i=0;i<soldseats.length;i++){
                soldList.add(soldseats[i]);
            }
        }
        for(String sold:soldList){
            for(int i=0;i<seatArrs.length;i++){
                if(sold.equalsIgnoreCase(seatArrs[i])){
                    return false;
                }
            }
        }
        return true;
    }
    //创建新订单
    @Transactional(rollbackFor=Exception.class)
    @Override
    public OrderVO saveOrderInfo(Integer fieldId, String soldSeats, String seatsName, Integer userId) {

//编号
        String uuid=UUIDUtil.genUuid();

//影片信息
        FilmInfoVO filmInfoVO = cinemaServiceAPI.getFilmInfoByFieldId(fieldId);
        Integer filmId= Integer.parseInt(filmInfoVO.getFilmId());

        //获取影院信息
        OrderQueryVO orderQueryVO = cinemaServiceAPI.getOrderNeeds(fieldId);
        Integer cinemaId=Integer.parseInt(orderQueryVO.getCinemaId());
        double filmPrice=Double.parseDouble(orderQueryVO.getFilmPrice());
        //求订单总金额
        String[] soldList=soldSeats.split(",");
        int soldNum=soldList.length;
        double totalPrice=getTotalPrice(soldNum,filmPrice);
        MoocOrder2019T moocOrderT=new MoocOrder2019T();
        moocOrderT.setUuid(uuid);
        moocOrderT.setSeatsName(seatsName);
        moocOrderT.setSeatsIds(soldSeats);
        moocOrderT.setOrderUser(userId);
        moocOrderT.setOrderPrice(totalPrice);
        moocOrderT.setFilmPrice(filmPrice);
        moocOrderT.setFilmId(filmId);
        moocOrderT.setFieldId(fieldId);
        moocOrderT.setCinemaId(cinemaId);
        int insert= moocOrder2019TMapper.insert(moocOrderT);
        if(insert>0){
            OrderVO orderVO= moocOrder2019TMapper.getOrderInfoById(uuid);
            String orderId=orderVO.getOrderId();
            System.err.println(orderId);
     //消息队列处理超时未支付订单
            rabbitSender.sendOrderId(orderId);
            if(orderVO.getOrderId()==null){
                log.error("订单信息查询失败,订单编号为{}",uuid);
                return null;
            }else {
                //返回查询结果
                return orderVO;
            }


        }else {
            //插入出错
            log.error("订单插入失败");
            return null;
        }

    }
    private double getTotalPrice(int solds,double filmPrice){
        BigDecimal soldsDeci=new BigDecimal(solds);
        BigDecimal filmPriceDeci=new BigDecimal(filmPrice);
        BigDecimal result=soldsDeci.multiply(filmPriceDeci);
        //四舍五入，取小数点后两位
        BigDecimal bigDecimal= result.setScale(2,RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
    @Override
    public Page<OrderVO> getOrderByUserId(Integer userId, Page<OrderVO> page) {
        Page<OrderVO> result=new Page<>(page.getCurrent(),page.getSize());
        if(userId==null){
            log.error("订单查询业务失败，用户编号未传入");
            return null;
        }else {
            List<OrderVO> ordersByUserId = moocOrder2019TMapper.getOrdersByUserId(userId);
            if(ordersByUserId==null&&ordersByUserId.size()==0){
                result.setTotal(0);
                result.setRecords(new ArrayList<>());
                return result;
            }else {
                //获取订单总数
                EntityWrapper<MoocOrder2019T> entityWrapper=new EntityWrapper<>();
                entityWrapper.eq("order_user",userId);
                Integer counts=  moocOrder2019TMapper.selectCount(entityWrapper);
                //将结果放入page
                result.setTotal(counts);
                result.setRecords(ordersByUserId);
                return result;
            }
        }


    }
    //根据放映查询，获取所有的已售座位
    /*
    1  1,2,3,4
    1 4,5,6
     */
    @Override
    public String getSoldSeatsByFieldId(Integer fieldId) {
        if(fieldId==null){
            log.error("查询表已售座位错误,未传入任何场次编号");
            return "";

        }else {
            String soldSeats= moocOrder2019TMapper.getSoldSeatsByFieldId(fieldId);
            return  soldSeats;
        }



    }

    @Override
    public OrderVO getOrderInfoById(String orderId) {
        OrderVO orderInfoById = moocOrder2019TMapper.getOrderInfoById(orderId);
        String seatsName = orderInfoById.getSeatsName();
        int ticketNum=1;

        for(int i=0;i<seatsName.length();i++){
            if(','==seatsName.charAt(i)){
                  ticketNum++;
            }
        }
        orderInfoById.setFilmPoster("http://img.gongyu91.cn"+orderInfoById.getFilmPoster());
        orderInfoById.setTicketNum(""+ticketNum);
        return orderInfoById;
    }

    @Override
    public boolean paySuccess(String orderId) {
        MoocOrder2019T moocOrderT=new MoocOrder2019T();
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(1);
        Integer integer= moocOrder2019TMapper.updateById(moocOrderT);
        if(integer>=1){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public boolean payFail(String orderId) {
        MoocOrder2019T moocOrderT=new MoocOrder2019T();
        moocOrderT.setUuid(orderId);
        moocOrderT.setOrderStatus(2);
        Integer integer= moocOrder2019TMapper.updateById(moocOrderT);
        if(integer>=1){
            return true;
        }else {
            return false;
        }
    }


}
