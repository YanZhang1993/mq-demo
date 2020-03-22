package com.joke.springboot.example.my;
import java.util.List;

import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerService {

    @EventListener(condition = "#event.msgs[0].topic=='bamen-public-topic-join' && #event.msgs[0].tags=='white'")
    public void rocketmqMsgListener(MessageEvent event) {
        try {
            List<MessageExt> msgs = event.getMsgs();
            for (MessageExt msg : msgs) {
                System.err.println("消费消息:"+new String(msg.getBody()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
