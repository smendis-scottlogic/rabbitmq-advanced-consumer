package com.example.rabbitmqconsumer.models;

import lombok.Data;

@Data
public class MyMessage {
    private final int messageId;

    private final String message;
}
