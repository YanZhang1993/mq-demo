package com.joke.springboot.example.my;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import org.springframework.web.bind.annotation.RestController;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {

    @Autowired
    private ProducerBean producer;

    /**
     * 发送普通消息
     */
    @GetMapping("/sendMessage")
    public void sendMsg() {

        for(int i=0;i<1;i++){
            User user = new User();
            user.setId(i);
            user.setUsername("jhp"+i);
            String json = JSON.toJSONString(user);
            Message msg = new Message("bamen-public-topic-join","white",json.getBytes());
            try {
                SendResult result = producer.send(msg);
                System.out.println("消息id:"+result.getMessageId()+":"+","+"发送状态:");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

}
