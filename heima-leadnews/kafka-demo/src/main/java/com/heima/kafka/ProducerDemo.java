package com.heima.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @Description:
 * @author: yp
 */
public class ProducerDemo {
    private static final String TOPIC = "wm_news_scan_topic";

    public static void main(String[] args) {
        //1.创建配置对象
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.211.136:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.RETRIES_CONFIG,10);
        //2.创建Producer对象
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        //3.创建消息封装对象
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC,"1");
        //4.发送
        producer.send(record);
        producer.close();
    }
}
