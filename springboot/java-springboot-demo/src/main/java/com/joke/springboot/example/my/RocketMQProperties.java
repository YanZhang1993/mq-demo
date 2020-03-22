package com.joke.springboot.example.my;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
//@PropertySource("classpath:config/rocketmq.properties")
@ConfigurationProperties(prefix = "joke.rocketmq")
@Configuration
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class RocketMQProperties {
    private String namesrvAddr;
    private String accessKey;
    private String secretKey;
    private boolean enableMsgTrace = true;
    private String customizedTraceTopic = "RMQ_SYS_TRACE_TOPIC";
    private String producerGroupName;
    private String transactionProducerGroupName;
    private String consumerGroupName;
    private String producerInstanceName;
    private String consumerInstanceName;
    private String producerTranInstanceName;
    private int consumerBatchMaxSize;
    private boolean consumerBroadcasting;
    private boolean enableHistoryConsumer;
    private boolean enableOrderConsumer;
    /**
     * Compress message body threshold, namely, message body larger than 4k will be compressed on default.
     */
    private int compressMessageBodyThreshold = 1024 * 4;
    /**
     * Indicate whether to retry another broker on sending failure internally.
     */
    private boolean retryNextServer = false;
    /**
     * 发送消息超时时间
     */
    private int sendMsgTimeout = 3000;
    /**
     * 发送同步消息失败后重试次数：2
     */
    private int retryTimesWhenSendFailed = 2;
    /**
     * 发送异步消息失败后重试次数：2
     */
    private int retryTimesWhenSendAsyncFailed = 2;
    /**
     * 单条消息的最大容量：4M
     */
    private int maxMessageSize = 1024 * 1024 * 4;
    /**
     * 消息压缩阈值：超过4k会被压缩
     */
    private int compressMsgBodyOverHowmuch = 1024 * 4;
    /**
     * 发送失败是否立即切换至其他broker
     */
    private boolean retryAnotherBrokerWhenNotStoreOk = false;
    private List<String> subscribe = new ArrayList<String>();
}
