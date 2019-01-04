package com.jetbrains.kafka.tool.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.Arrays;
import java.util.Objects;

import com.jetbrains.kafka.tool.collection.FixedStack;
import com.jetbrains.kafka.tool.service.model.ProducerHistoryEntry;
import org.jetbrains.annotations.Nullable;

@State(
        name = "KafkaToolPersistentStateComponent",
        storages = {@Storage("kafka-tool-state.xml")}
)
public class KafkaToolPersistentStateComponent implements PersistentStateComponent<KafkaToolPersistentStateComponent> {
    private String bootstrapServers = "localhost:9092";
    private FixedStack<ProducerHistoryEntry> producerHistoryDataStack = FixedStack.newFixedStack();
    private String schemaRegistryUrl = "http://localhost:8081";
    private String avroPackagePrefix = "com.company";
    private String serializer = "KafkaAvroSerializer";

    public KafkaToolPersistentStateComponent() {
    }

    @Nullable
    public KafkaToolPersistentStateComponent getState() {
        return this;
    }

    public void loadState(KafkaToolPersistentStateComponent state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setProducerHistoryDataStack(FixedStack<ProducerHistoryEntry> producerHistoryDataStack) {
        this.producerHistoryDataStack = producerHistoryDataStack;
    }

    public FixedStack<ProducerHistoryEntry> getProducerHistoryDataStack() {
        return this.producerHistoryDataStack;
    }

    public String getSchemaRegistryUrl() {
        return this.schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public void addProducerHistoryEntry(ProducerHistoryEntry producerHistoryEntry) {
        this.producerHistoryDataStack.push(producerHistoryEntry);
    }

    public String getAvroPackagePrefix() {
        return this.avroPackagePrefix;
    }

    public void setAvroPackagePrefix(String avroPackagePrefix) {
        this.avroPackagePrefix = avroPackagePrefix;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getSerializer() {
        return this.serializer;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            KafkaToolPersistentStateComponent component = (KafkaToolPersistentStateComponent)o;
            return Objects.equals(this.bootstrapServers, component.bootstrapServers) && Objects.equals(this.producerHistoryDataStack, component.producerHistoryDataStack) && Objects.equals(this.schemaRegistryUrl, component.schemaRegistryUrl) && Objects.equals(this.avroPackagePrefix, component.avroPackagePrefix) && Objects.equals(this.serializer, component.serializer);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.bootstrapServers, Arrays.hashCode(this.producerHistoryDataStack.toArray()), this.schemaRegistryUrl, this.avroPackagePrefix, this.serializer});
    }
}
