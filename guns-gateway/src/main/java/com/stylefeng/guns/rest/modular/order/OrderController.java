package com.stylefeng.guns.rest.modular.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.alipay.AliPayServiceAPI;
import com.stylefeng.guns.api.alipay.vo.AliPayResultVO;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.TokenBucket;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.order.cache.JedisUtil;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order/")
public class OrderController {
    private static     TokenBucket tokenBucket=new TokenBucket();
    private static final String IMG_PRE="http://img.meetingshop.cn/";

    @Reference(interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2019",filter = "tracing")
    private OrderServiceAPI orderServiceAPI;
    @Reference(interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2018")
    private OrderServiceAPI orderServiceAPI2018;
    @Reference(interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2017")
    private OrderServiceAPI orderServiceAPI2017;
    @Reference(interfaceClass = AliPayServiceAPI.class,check = false,filter = "tracing")
    private AliPayServiceAPI aliPayServiceAPI;
    @Autowired
    private JedisUtil.Sets jedisSets;
    private final static String BUY_TICKETS_KEY="buy_ticket_key:";
    //方法名随便起，返回值和参数一定要一样
    public ResponseVO error(Integer fieldId,String soldSeats,String seatsName){
        return ResponseVO.serviceFail("抱歉，下单的人太多了，请稍后重试");
    }
    //购票
    /*
    信号量隔离
    线程池隔离
    线程切换
     */
//    @HystrixCommand(fallbackMethod = "error", commandProperties = {
//            @HystrixProperty(name="execution.isolation.strategy", value = "THREAD"),
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value
//                    = "4000"),
//            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
//            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},
//                     threadPoolProperties = {
//                    @HystrixProperty(name = "coreSize", value = "1"),
//                    @HystrixProperty(name = "maxQueueSize", value = "10"),
//                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
//                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
//                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
//                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
//            })
    @RequestMapping(value = "buyTickets",method = RequestMethod.POST)
    public ResponseVO buyTickets(Integer fieldId,String soldSeats,String seatsName) {
        //使用redis sets避免重复提交
        long sadd = jedisSets.sadd(BUY_TICKETS_KEY+CurrentUser.getCurrentUser(),"0" );
        jedisSets.expired(BUY_TICKETS_KEY+CurrentUser.getCurrentUser(),5);
        if(sadd==0){
            return ResponseVO.serviceFail("请勿重复提交!");
        }
        try{


            if(tokenBucket.getToken()){
//验证售出的票是否为真
                if(orderServiceAPI.isTrueSeats(fieldId+"",soldSeats)){
                    //已经销售的座位，有没有这些座位
                    if( orderServiceAPI.isNotSoldSeats(fieldId+"",soldSeats)){
                        //创建订单信息，主要获取登录人
                        OrderVO orderVO=  orderServiceAPI.saveOrderInfo(fieldId,soldSeats,seatsName,Integer.valueOf(CurrentUser.getCurrentUser()));

                        if(orderVO==null){
                            log.error("购票未成功");
                            return ResponseVO.serviceFail("购票业务异常");
                        }else {
                            return ResponseVO.success(orderVO);
                        }
                    }else {
                        return ResponseVO.serviceFail("订单中的座位重复");
                    }

                }else {
                    return ResponseVO.serviceFail("订单中的座位有异常");
                }
            }else {
                return ResponseVO.serviceFail("购票人数过多，请稍后再试");
            }


        }catch (Exception e){
            log.error("购票业务异常",e);
            return ResponseVO.serviceFail("购票业务异常");
        }



        //创建订单信息
    }


    @RequestMapping(value = "getOrderInfo",method = RequestMethod.POST)
    public ResponseVO getOrderInfo(@RequestParam(name = "nowPage",required = false,defaultValue = "1") Integer nowPage,
                                   @RequestParam(name = "pageSize",required = false,defaultValue = "5")Integer pageSize){
//获取当前登录人的信息
        String userId= CurrentUser.getCurrentUser();

        //使用当前登录人获取已经购买的订单
        Page<OrderVO> page=new Page<>(nowPage,pageSize);
        if(userId!=null&&userId.trim().length()>0){
            Page<OrderVO> result= orderServiceAPI.getOrderByUserId(Integer.parseInt(userId),page);
            Page<OrderVO> result2018= orderServiceAPI2018.getOrderByUserId(Integer.parseInt(userId),page);
            Page<OrderVO> result2017= orderServiceAPI2017.getOrderByUserId(Integer.parseInt(userId),page);
            //合并结果

            int counts= (int) (result.getTotal()+result2017.getTotal()+result2018.getTotal());
            //2017和2018的订单总数合并
            List<OrderVO> orderVOList=new ArrayList<>();
            orderVOList.addAll(result.getRecords());
            orderVOList.addAll(result2018.getRecords());
            orderVOList.addAll(result2017.getRecords());
            return ResponseVO.success(nowPage,counts,"",orderVOList);
        }else {
            return  ResponseVO.serviceFail("用户未登录");
        }


    }
    @RequestMapping(value = "getPayInfo",method = RequestMethod.POST)
    public ResponseVO getPayInfo(@RequestParam("orderId") String orderId, HttpServletRequest request, HttpServletResponse response){
        AlipayClient alipayClient = new DefaultAlipayClient("","","","json", "utf-8", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmIFZp8s5KErhfu3Pv6/vIuD8EOqIT51vA/rSz+g50wHi2oriK8tRfN7vWhwI9CbKXLBkh7ZQFr+vi/4nt2Pkvpaha32XRU/nxEwzfPZvoirJIhfYNr1QNtffzTmDBGp35y+rCLs5p1vXnUO7wS4N3C0o0OZM+nRddyg0JY0usi70OjwThwo+rcG+xZqq75ugggEVrQl2TWfnAyjKskg7eCof8zVUxP3OKUS+etGNyClgn/v0aj+cG4JyMHpu6E7JZ/Z9YdFjH9IaiSySxaXA7A7UrWGAarK0IEp6gLfvEa6OPX0alUzaior3Cw94IMzqdhlo1Uokr+IWivw6ivct2QIDAQAB","RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl("http://film.gongyu91.cn/paysuccess");
        OrderVO orderVO=orderServiceAPI.getOrderInfoById(orderId);
        String filmName=orderVO.getFilmName();
        String totalAmount= orderVO.getOrderPrice();
       String ticketNum= orderVO.getTicketNum();

//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址

        AlipayTradeAppPayModel model=new AlipayTradeAppPayModel();
        model.setBody("购买"+filmName+"票"+ticketNum+"张");
        model.setSubject(filmName+"电影票");
        model.setTotalAmount(totalAmount);
        model.setOutTradeNo(orderId);
        model.setTimeoutExpress("10m");
        alipayRequest.setBizModel(model);
        String form = null; //调用SDK生成表单
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
            return ResponseVO.success(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
return ResponseVO.serviceFail("返回失败");
        //获取当前登录人的信息
//        String userId= CurrentUser.getCurrentUser();
//        if(userId==null||userId.trim().length()==0){
//            return ResponseVO.serviceFail("抱歉，用户未登录");
//        }
        //订单二维码返回结果
//        try {
//            aliPayServiceAPI.getQRCode(orderId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//return ResponseVO.success(IMG_PRE,aliPayInfoVO);
    }

    @RequestMapping(value = "getPayResult",method = RequestMethod.POST)
    public ResponseVO getPayResult(@RequestParam("orderId") String orderId,@RequestParam(value = "tryNums",required = false,defaultValue = "1")Integer tryNums){
        //获取当前登录人的信息
        String userId= CurrentUser.getCurrentUser();
        if(userId==null||userId.trim().length()==0){
            return ResponseVO.serviceFail("抱歉，用户未登录");
        }
        //判断是否支付超时
        if(tryNums>=4){
            return ResponseVO.serviceFail("订单支付失败，请稍后重试");
        }else {
            AliPayResultVO aliPayResultVO=aliPayServiceAPI.getOrderStatus(orderId);
            if(aliPayResultVO==null||ToolUtil.isEmpty(aliPayResultVO.getOrderId())){
                AliPayResultVO serviceFailVO=new AliPayResultVO();
                serviceFailVO.setOrderId(orderId);
                serviceFailVO.setOrderStatus(0);
                serviceFailVO.setOrderMsg("支付不成功");
                return ResponseVO.success(serviceFailVO);
            }
            return ResponseVO.success(aliPayResultVO);
        }

    }

    //选座购买后返回的订单信息
    @RequestMapping(value = "getCurrentOrderInfo",method = RequestMethod.GET)
    public ResponseVO getCurrentOrderInfo(String orderId){
        OrderVO orderInfoById = orderServiceAPI.getOrderInfoById(orderId);
        return ResponseVO.success(orderInfoById);
    }

}
