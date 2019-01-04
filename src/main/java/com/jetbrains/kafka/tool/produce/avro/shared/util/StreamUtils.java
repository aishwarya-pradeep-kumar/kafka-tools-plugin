package com.jetbrains.kafka.tool.produce.avro.shared.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
    public StreamUtils() {
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        return asStream(sourceIterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        return asStream(() -> {
            return sourceIterator;
        }, parallel);
    }

    public static <T> Stream<T> asStream(Iterable<T> sourceIterable) {
        return asStream(sourceIterable, false);
    }

    public static <T> Stream<T> asStream(Iterable<T> sourceIterable, boolean parallel) {
        return StreamSupport.stream(sourceIterable.spliterator(), parallel);
    }
}

