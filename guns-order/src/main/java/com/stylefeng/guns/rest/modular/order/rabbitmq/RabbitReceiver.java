package com.stylefeng.guns.rest.modular.order.rabbitmq;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.rabbitmq.client.Channel;
import com.stylefeng.guns.rest.common.persistence.dao.MessageExceptionMapper;
import com.stylefeng.guns.rest.common.persistence.dao.MessageManageMapper;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrder2019TMapper;
import com.stylefeng.guns.rest.common.persistence.model.MessageManage;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrder2019T;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RabbitReceiver  {
    @Autowired
    private MoocOrder2019TMapper moocOrder2019TMapper;
    @Autowired
    private MessageExceptionMapper messageExceptionMapper;
    @Autowired
    private MessageManageMapper messageManageMapper;

    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value="order_id_overtime_dead",durable="true"),
            exchange = @Exchange(value = "order_id_dead",durable = "true",type = "direct",ignoreDeclarationExceptions = "true"),
            key="order.id.overtime.dead"
    ))
    @RabbitHandler
    public  void onMessage(Message message, Channel channel)throws Exception{
        try{
            System.err.println("线程id: "+Thread.currentThread().getId()+",线程名: "+Thread.currentThread().getName());
            System.err.println("--------------------------");

            System.err.println("消费端 :"+message.getPayload());

            String orderId = (String) message.getPayload();

            String messageId = message.getHeaders().get("messageId")+"";

            EntityWrapper<MoocOrder2019T> entityWrapper=new EntityWrapper<>();

            System.err.println("全局唯一id"+messageId);

            entityWrapper.eq("UUID",orderId);

            moocOrder2019TMapper.delete(entityWrapper);

            Long deliveryTag=(Long)message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
            //手工ACK
            //deleveryTag表示该信道下消息的唯一标识


            channel.basicAck(deliveryTag,false);

        }catch (Exception e){
            MessageManage messageManage=new MessageManage();
            System.err.println(message.getHeaders().get("messageId"));
            messageManage.setId(message.getHeaders().get("messageId")+"");
            System.err.println(message.getPayload());
            messageManage.setOrderId((String)message.getPayload());
            messageManage.setStauts(0);

            messageManageMapper.insertAllColumn(messageManage);
           log.error("消息ID: {},异常信息: {}",(String) message.getHeaders().get("messageId"),e.getMessage());
            channel.basicAck((Long)message.getHeaders().get(AmqpHeaders.DELIVERY_TAG),false);
        }

    }



//    @RabbitListener(bindings = @QueueBinding(
//            value=@Queue(value="order_id_overtime",durable="true",arguments = {@Argument(name = "x-dead-letter-exchange",value = "order_id_dead"),@Argument(name = "x-dead-letter-routing-key",value = "order.id.overtime.dead")}),
//            exchange = @Exchange(value = "order_id",durable = "true",type = "direct",ignoreDeclarationExceptions = "true"),
//            key="order.id.overtime"))
//    @RabbitHandler
//    public  void onOrderMessage(Message message,Channel channel)throws Exception{
//        System.err.println("--------------------------");
//        Long deliveryTag=(Long)message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
//        channel.basicAck(deliveryTag,false);
//    }
}
