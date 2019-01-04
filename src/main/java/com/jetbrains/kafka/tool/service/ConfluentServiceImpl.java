package com.jetbrains.kafka.tool.service;

import com.google.inject.Inject;
import com.intellij.notification.Notifications.Bus;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.jetbrains.kafka.tool.ui.notification.TopicRefreshTimeoutNotification;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConfluentServiceImpl implements ConfluentService {
    @Inject
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;

    public ConfluentServiceImpl() {
    }

    public Set<String> getTopicList() {
        Properties props = new Properties();
        props.put("bootstrap.servers", this.kafkaToolPersistentStateComponent.getBootstrapServers());
        props.put("group.id", "kafka-tool-topic-registry");
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);
        props.put("exclude.internal.topics", true);
        props.put("request.timeout.ms", 1000);
        props.put("session.timeout.ms", 500);
        props.put("heartbeat.interval.ms", 250);

        try {
            KafkaConsumer<String, String> consumer = new KafkaConsumer(props);
            Throwable var3 = null;

            Set var5;
            try {
                Set<String> strings = consumer.listTopics().keySet();
                var5 = (Set)strings.stream().sorted().collect(Collectors.toSet());
            } catch (Throwable var15) {
                var3 = var15;
                throw var15;
            } finally {
                if (consumer != null) {
                    if (var3 != null) {
                        try {
                            consumer.close();
                        } catch (Throwable var14) {
                            var3.addSuppressed(var14);
                        }
                    } else {
                        consumer.close();
                    }
                }

            }

            return var5;
        } catch (TimeoutException var17) {
            Bus.notify(TopicRefreshTimeoutNotification.getInstance());
            return new HashSet<>();
        }
    }
}
