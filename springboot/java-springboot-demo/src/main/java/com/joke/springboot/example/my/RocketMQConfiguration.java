package com.joke.springboot.example.my;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.SendMessageTraceHookImpl;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@Slf4j
public class RocketMQConfiguration {

    @Autowired
    private RocketMQProperties rocketMQProperties;

    //事件监听
    @Autowired
    private ApplicationEventPublisher publisher = null;

    private static boolean isFirstSub = true;

    private static long startTime = System.currentTimeMillis();


    @PostConstruct
    public void init() {
        System.err.println(rocketMQProperties.getNamesrvAddr());
        System.err.println(rocketMQProperties.getProducerGroupName());
        System.out.println(rocketMQProperties.getAccessKey());
        System.out.println(rocketMQProperties.getSecretKey());
        System.err.println(rocketMQProperties.getConsumerBatchMaxSize());
        System.err.println(rocketMQProperties.getConsumerGroupName());
        System.err.println(rocketMQProperties.getConsumerInstanceName());
        System.err.println(rocketMQProperties.getProducerInstanceName());
        System.err.println(rocketMQProperties.getProducerTranInstanceName());
        System.err.println(rocketMQProperties.getTransactionProducerGroupName());
        System.err.println(rocketMQProperties.isConsumerBroadcasting());
        System.err.println(rocketMQProperties.isEnableHistoryConsumer());
        System.err.println(rocketMQProperties.isEnableOrderConsumer());
        System.out.println(rocketMQProperties.getSubscribe().get(0));
    }

    /**
     * 生产者
     * @return
     */
    @Bean
    @ConditionalOnClass(DefaultMQProducer.class)
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    @ConditionalOnProperty(prefix = "joke.rocketmq", value = {"namesrvAddr", "producerGroupName"})
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        String nameServer = rocketMQProperties.getNamesrvAddr();
        String groupName = rocketMQProperties.getProducerGroupName();
        Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
        Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");


        String ak = rocketMQProperties.getAccessKey();
        String sk = rocketMQProperties.getSecretKey();
        boolean isEnableMsgTrace = rocketMQProperties.isEnableMsgTrace();
        String customizedTraceTopic = rocketMQProperties.getCustomizedTraceTopic();

        DefaultMQProducer producer = createDefaultMQProducer(groupName, ak, sk, isEnableMsgTrace, customizedTraceTopic);

        producer.setNamesrvAddr(nameServer);
        producer.setAccessChannel(AccessChannel.CLOUD);
        producer.setSendMsgTimeout(rocketMQProperties.getSendMsgTimeout());
        producer.setRetryTimesWhenSendFailed(rocketMQProperties.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(rocketMQProperties.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(rocketMQProperties.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(rocketMQProperties.getCompressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(rocketMQProperties.isRetryNextServer());

        return producer;
    }

    public static DefaultMQProducer createDefaultMQProducer(String groupName, String ak, String sk,
                                                            boolean isEnableMsgTrace, String customizedTraceTopic) {

        boolean isEnableAcl = !StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk);
        DefaultMQProducer producer;
        if (isEnableAcl) {
            producer = new TransactionMQProducer(groupName, new AclClientRPCHook(new SessionCredentials(ak, sk)));
            producer.setVipChannelEnabled(false);
        } else {
            producer = new TransactionMQProducer(groupName);
        }

        if (isEnableMsgTrace) {
            try {
                AsyncTraceDispatcher dispatcher = new AsyncTraceDispatcher(customizedTraceTopic, isEnableAcl ? new AclClientRPCHook(new SessionCredentials(ak, sk)) : null);
                dispatcher.setHostProducer(producer.getDefaultMQProducerImpl());
                Field field = DefaultMQProducer.class.getDeclaredField("traceDispatcher");
                field.setAccessible(true);
                field.set(producer, dispatcher);
                producer.getDefaultMQProducerImpl().registerSendMessageHook(
                        new SendMessageTraceHookImpl(dispatcher));
            } catch (Throwable e) {
                log.error("system trace hook init failed ,maybe can't send msg trace data");
            }
        }

        return producer;
    }


    /**
     * 创建普通消息发送者实例
     * @return
     * @throws MQClientException
     */
//    @Bean
//    public DefaultMQProducer defaultProducer() throws MQClientException {
//        DefaultMQProducer producer = new DefaultMQProducer(
//                rocketMQProperties.getProducerGroupName(), new AclClientRPCHook(
//                        new SessionCredentials(rocketMQProperties.getAccessKey(), rocketMQProperties.getSecretKey())),
//                rocketMQProperties.isEnableMsgTrace(), rocketMQProperties.getCustomizedTraceTopic());
//        producer.setAccessChannel(AccessChannel.CLOUD);
//        producer.setNamesrvAddr(rocketMQProperties.getNamesrvAddr());
//        producer.setInstanceName(rocketMQProperties.getProducerInstanceName());
//        producer.setVipChannelEnabled(false);
//        producer.setRetryTimesWhenSendAsyncFailed(10);
//        producer.start();
//        log.info("rocketmq producer server is starting....");
//        return producer;
//    }

    /**
     * 创建支持消息事务发送的实例
     * @return
     * @throws MQClientException
     */
//    @Bean
//    public TransactionMQProducer transactionProducer() throws MQClientException {
//        TransactionMQProducer producer = new TransactionMQProducer(
//                rocketMQProperties.getTransactionProducerGroupName());
//        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
//        producer.setInstanceName(rocketMQProperties
//                .getProducerTranInstanceName());
//        producer.setRetryTimesWhenSendAsyncFailed(10);
//        // 事务回查最小并发数
//        producer.setCheckThreadPoolMinSize(2);
//        // 事务回查最大并发数
//        producer.setCheckThreadPoolMaxSize(2);
//        // 队列数
//        producer.setCheckRequestHoldMax(2000);
//        producer.start();
//        log.info("rocketmq transaction producer server is starting....");
//        return producer;
//    }

    /**
     * 创建消息消费的实例
     * @return
     * @throws MQClientException
     */
    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("",
                rocketMQProperties.getConsumerGroupName(), new AclClientRPCHook(
                new SessionCredentials(rocketMQProperties.getAccessKey(), rocketMQProperties.getSecretKey())));
        consumer.setNamesrvAddr(rocketMQProperties.getNamesrvAddr());
        consumer.setInstanceName(rocketMQProperties.getConsumerInstanceName());

        //判断是否是广播模式
        if (rocketMQProperties.isConsumerBroadcasting()) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        }
        //设置批量消费
        consumer.setConsumeMessageBatchMaxSize(rocketMQProperties
                .getConsumerBatchMaxSize() == 0 ? 1 : rocketMQProperties
                .getConsumerBatchMaxSize());

        //获取topic和tag
        List<String> subscribeList = rocketMQProperties.getSubscribe();
        for (String sunscribe : subscribeList) {
            consumer.subscribe(sunscribe.split(":")[0], sunscribe.split(":")[1]);
        }

        // 顺序消费
        if (rocketMQProperties.isEnableOrderConsumer()) {
            consumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(
                        List<MessageExt> msgs, ConsumeOrderlyContext context) {
                    try {
                        context.setAutoCommit(true);
                        msgs = filterMessage(msgs);
                        if (msgs.size() == 0)
                            return ConsumeOrderlyStatus.SUCCESS;
                        publisher.publishEvent(new MessageEvent(msgs, consumer));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });
        }
        // 并发消费
        else {

            consumer.registerMessageListener(new MessageListenerConcurrently() {

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(
                        List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    try {
                        //过滤消息
                        msgs = filterMessage(msgs);
                        if (msgs.size() == 0)
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                        publisher.publishEvent(new MessageEvent(msgs, consumer));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);

                    try {
                        consumer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.info("rocketmq consumer server is starting....");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        return consumer;
    }

    /**
     * 消息过滤
     * @param msgs
     * @return
     */
    private List<MessageExt> filterMessage(List<MessageExt> msgs) {
        if (isFirstSub && !rocketMQProperties.isEnableHistoryConsumer()) {
            msgs = msgs.stream()
                    .filter(item -> startTime - item.getBornTimestamp() < 0)
                    .collect(Collectors.toList());
        }
        if (isFirstSub && msgs.size() > 0) {
            isFirstSub = false;
        }
        return msgs;
    }

}
