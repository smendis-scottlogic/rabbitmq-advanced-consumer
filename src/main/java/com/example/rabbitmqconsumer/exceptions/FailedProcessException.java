package com.example.rabbitmqconsumer.exceptions;

public class FailedProcessException extends Exception{
    public FailedProcessException(String message){
        super(message);
    }
}
