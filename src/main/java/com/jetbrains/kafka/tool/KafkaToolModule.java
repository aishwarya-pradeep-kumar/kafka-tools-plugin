package com.jetbrains.kafka.tool;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.*;
import com.google.inject.Module;
import com.intellij.openapi.components.ServiceManager;
import com.jetbrains.kafka.tool.service.*;
import com.jetbrains.kafka.tool.service.shared.DefaultJsonToAvroConverter;
import com.jetbrains.kafka.tool.ui.KafkaProducerComponent;
import com.jetbrains.kafka.tool.ui.KafkaSettingsComponent;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class KafkaToolModule extends AbstractModule {

    protected static final Supplier<Injector> injector = Suppliers.memoize(() -> {
        return Guice.createInjector(new Module[]{new KafkaToolModule()});
    });

    public static <T> T getInstance(Class<T> type) {
        return (injector.get()).getInstance(type);
    }

    protected KafkaToolModule() {
    }

    protected void configure() {
        Provider<KafkaToolPersistentStateComponent> persistentStateProvider = () -> {
            return (KafkaToolPersistentStateComponent)ServiceManager.getService(KafkaToolPersistentStateComponent.class);
        };
        this.bind(KafkaToolPersistentStateComponent.class).toProvider(persistentStateProvider).in(Singleton.class);
        Provider<KafkaToolProducerCache> kafkaToolProducerCacheProvider = () -> {
            return (KafkaToolProducerCache)ServiceManager.getService(KafkaToolProducerCache.class);
        };
        this.bind(KafkaToolProducerCache.class).toProvider(kafkaToolProducerCacheProvider).in(Singleton.class);
        this.bind(AvroClassScannerImpl.class).asEagerSingleton();
        this.bind(ConfluentServiceImpl.class).asEagerSingleton();
        this.bind(DefaultJsonToAvroConverter.class).asEagerSingleton();
        this.bind(KafkaProducerProxy.class).asEagerSingleton();
        this.bind(KafkaToolSerializerService.class).asEagerSingleton();
        this.bind(KafkaSettingsComponent.class);
        this.bind(KafkaProducerComponent.class);
    }
}