package com.joke.springboot.example.normal;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

@Component
public class DemoMessageListener implements MessageListener {

    @Override
    public Action consume(Message message, ConsumeContext context) {

        System.out.println("Receive: " + message);
        try {
            System.out.println("=============="+ JSON.parseObject(message.getBody(),String.class));
            return Action.CommitMessage;
        } catch (Exception e) {
            //消费失败
            return Action.ReconsumeLater;
        }
    }
}
