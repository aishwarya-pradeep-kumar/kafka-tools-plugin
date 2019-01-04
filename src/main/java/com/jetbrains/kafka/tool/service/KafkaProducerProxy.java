package com.jetbrains.kafka.tool.service;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jetbrains.kafka.tool.exception.KafkaToolProducerException;
import org.apache.avro.AvroTypeException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerProxy implements ProducerProxy<String, Object> {
    @Inject
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;
    @Inject
    private KafkaToolSerializerService kafkaToolSerializerService;
    private KafkaProducer<String, Object> kafkaProducer;

    public KafkaProducerProxy() {
    }

    public void reset() {
        if (Objects.nonNull(this.kafkaProducer)) {
            this.kafkaProducer.close();
        }

        Class<? extends Serializer> serializer = this.kafkaToolSerializerService.getSerializerByKey(this.kafkaToolPersistentStateComponent.getSerializer());
        Map<String, Object> props = new HashMap();
        props.put("bootstrap.servers", this.kafkaToolPersistentStateComponent.getBootstrapServers());
        props.put("schema.registry.url", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", serializer);
        props.put("acks", "1");
        props.put("max.block.ms", 250);
        this.kafkaProducer = new KafkaProducer(props);
    }

    public void send(String topic, String key, Object object) {
        try {
            ProducerRecord<String, Object> producerRecord = new ProducerRecord(topic, key, object);
            this.kafkaProducer.metrics();
            this.kafkaProducer.send(producerRecord);
        } catch (TimeoutException | AvroTypeException var5) {
            throw new KafkaToolProducerException(var5);
        }
    }
}
