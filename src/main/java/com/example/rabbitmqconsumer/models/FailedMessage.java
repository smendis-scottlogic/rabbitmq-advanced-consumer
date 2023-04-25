package com.example.rabbitmqconsumer.models;

import lombok.Data;

@Data
public class FailedMessage {
    private final int messageId;

    private final String message;

    private final String error;
}
