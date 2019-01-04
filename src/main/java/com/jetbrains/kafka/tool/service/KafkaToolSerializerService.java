package com.jetbrains.kafka.tool.service;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;

import io.confluent.kafka.serializers.*;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaToolSerializerService {
    private static final Map<String, Class<? extends Serializer>> SERIALIZER_MAP = ImmutableMap.of("KafkaAvroSerializer", KafkaAvroSerializer.class, "StringSerializer", StringSerializer.class);

    public KafkaToolSerializerService() {
    }

    public Class<? extends Serializer> getSerializerByKey(String key) {
        return (Class)SERIALIZER_MAP.get(key);
    }

    public Set<String> getAllSerializers() {
        return SERIALIZER_MAP.keySet();
    }
}