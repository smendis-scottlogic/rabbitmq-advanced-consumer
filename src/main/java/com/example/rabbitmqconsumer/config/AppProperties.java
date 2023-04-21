package com.example.rabbitmqconsumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rabbitmq")
public class AppProperties {
    private String directExchange;

    private String directQueue;

    private int retryAttempts;

    private int backoffInterval;

    private int backoffMultiplier;

    private int backoffMaxInterval;

    private int concurrentConsumers;

    private int maxConcurrentConsumers;

    public String getDirectDeadLetterQueue() {
        return this.directQueue + ".dlq";
    }
}
