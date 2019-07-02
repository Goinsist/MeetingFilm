package com.stylefeng.guns.rest.modular.order.rabbitmq;


import com.stylefeng.guns.rest.common.persistence.dao.MessageExceptionMapper;
import com.stylefeng.guns.rest.common.persistence.model.MessageException;
import com.stylefeng.guns.rest.common.util.SnowFlake;
import com.stylefeng.guns.rest.modular.order.rabbitmq.cache.JedisUtil;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MessageExceptionMapper messageExceptionMapper;
    @Autowired
    private JedisUtil.Strings jedisStrings;
    @Autowired
    private JedisUtil.Keys jedisKeys;
    final RabbitTemplate.ConfirmCallback confirmCallback= new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            System.err.println("correlationData: "+correlationData);
            System.err.println("ack: "+ack);
            if(!ack){
                System.err.println("异常处理.....");
            }else {
                jedisKeys.del(correlationData.getId());
            }
        }
    };
    final RabbitTemplate.ReturnCallback returnCallback=new RabbitTemplate.ReturnCallback() {
        @Override
        public void returnedMessage(org.springframework.amqp.core.Message message, int i, String s, String s1, String s2) {
            MessageException messageException=new MessageException();
            messageException.setStatus(0);
            messageException.setOrderId(new String(message.getBody()));
            messageException.setId(String.valueOf((Integer) message.getMessageProperties().getHeaders().get("messageId")));
            messageExceptionMapper.insert(messageException);
            System.err.println("return exchange :"+s1+",routingKey :"+s2+",replayCode :"+i+",replayText :"+s);
        }
    };
    public void sendOrderId(String orderId){

        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
//            设置编码
            messageProperties.setContentEncoding("utf-8");
//            设置过期时间10*1000毫秒
            messageProperties.setExpiration("600000");
            return message;
        };
//生成全局唯一消息id
        SnowFlake snowFlake = new SnowFlake(1, 1);
        long messageId = snowFlake.nextId();
        jedisStrings.set(messageId+"",orderId);
        Map<String,Object> properties=new HashMap<>();
        properties.put("messageId",messageId+"");
        MessageHeaders mhs=new MessageHeaders(properties);
        Message msg=MessageBuilder.createMessage(orderId,mhs);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        CorrelationData correlationData=new CorrelationData();
        correlationData.setId(messageId+"");
        rabbitTemplate.convertAndSend("order_id","order.id.overtime",msg,messagePostProcessor,correlationData);
    }

//    public void sendOrder(String orderId)throws Exception{
//
//        rabbitTemplate.setConfirmCallback(confirmCallback);
//        rabbitTemplate.setReturnCallback(returnCallback);
//        CorrelationData correlationData=new CorrelationData();
//        String messageUid = UUIDUtil.genUuid();
//        correlationData.setId(messageUid);//id+时间戳保证消息全局唯一
//        rabbitTemplate.convertAndSend("order_id","order_id_overtime",orderId,correlationData);
//    }
}
