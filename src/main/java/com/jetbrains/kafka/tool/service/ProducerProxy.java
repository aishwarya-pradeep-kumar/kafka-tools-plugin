package com.jetbrains.kafka.tool.service;

public interface ProducerProxy<K, V> {
    void send(String var1, K var2, V var3) throws Exception;

    void reset();
}
