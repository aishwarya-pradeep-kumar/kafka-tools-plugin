package com.jetbrains.kafka.tool.service;

import java.util.Set;

public interface ConfluentService {
    Set<String> getTopicList();
}