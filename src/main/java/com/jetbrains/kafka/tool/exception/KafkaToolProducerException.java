package com.jetbrains.kafka.tool.exception;

public class KafkaToolProducerException extends RuntimeException {
    public KafkaToolProducerException(Throwable cause) {
        super(cause);
    }

    public KafkaToolProducerException(String message) {
        super(message);
    }

    public KafkaToolProducerException(String message, Throwable cause) {
        super(message, cause);
    }
}