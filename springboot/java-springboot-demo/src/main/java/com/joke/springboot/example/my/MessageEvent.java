package com.joke.springboot.example.my;
import lombok.Getter;
import lombok.Setter;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.context.ApplicationEvent;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Getter
@Setter
public class MessageEvent  extends ApplicationEvent{
    private static final long serialVersionUID = -4468405250074063206L;
    private DefaultMQPushConsumer consumer;
    private List<MessageExt> msgs;

    public MessageEvent(List<MessageExt> msgs, DefaultMQPushConsumer consumer) throws Exception {
        super(msgs);
        this.consumer = consumer;
        this.setMsgs(msgs);
    }
}
