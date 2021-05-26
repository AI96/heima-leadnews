package com.heima.common.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

@Data
@Configuration
@PropertySource("classpath:mongo.properties")
@ConfigurationProperties(prefix="mongo")
public class MongoDBconfigure {

    private String host;
    private Integer port;
    private String dbname;

    @Bean
    public MongoTemplate getMongoTemplate() {
        return new MongoTemplate(getSimpleMongoDbFactory());
    }

    public SimpleMongoDbFactory getSimpleMongoDbFactory() {
        return new SimpleMongoDbFactory(new MongoClient(host, port), dbname);
    }

}