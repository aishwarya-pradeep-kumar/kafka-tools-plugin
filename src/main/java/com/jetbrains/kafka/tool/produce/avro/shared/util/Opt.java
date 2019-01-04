package com.jetbrains.kafka.tool.produce.avro.shared.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Opt<T> {
    private static final Runnable NOOP = () -> {
    };
    private final T o;

    private Opt(T o) {
        this.o = o;
    }

    public boolean isPresent() {
        return this.o != null;
    }

    public T get() {
        return this.o;
    }

    public T orElse(T other) {
        return this.isPresent() ? this.o : other;
    }

    public Opt<T> ifPresent(Consumer<T> _if) {
        if (this.isPresent()) {
            _if.accept(this.get());
        }

        return this;
    }

    public Opt<T> ifPresent(Consumer<T> _if, Runnable _else) {
        if (this.isPresent()) {
            _if.accept(this.get());
        } else {
            _else.run();
        }

        return this;
    }

    public void notPresent(Runnable _else) {
        if (!this.isPresent()) {
            _else.run();
        }

    }

    public <MappingT> Opt<MappingT> map(Function<T, MappingT> _map) {
        return this.isPresent() ? of(_map.apply(this.get())) : empty();
    }

    public <MappingT> Opt<MappingT> flatMap(Function<T, Opt<MappingT>> _map) {
        return this.isPresent() ? of(((Opt)_map.apply(this.get())).get()) : empty();
    }

    public Opt<T> filter(Function<T, Boolean> _filter) {
        return this.isPresent() && (Boolean)_filter.apply(this.get()) ? this : empty();
    }

    public Opt<T> filter(Function<T, Boolean> _filter, Consumer<T> _else) {
        boolean filtered = this.isPresent() && (Boolean)_filter.apply(this.get());
        if (filtered) {
            _else.accept(this.get());
        }

        return filtered ? this : empty();
    }

    public static <StaticT> Opt<StaticT> empty() {
        return (Opt<StaticT>) of((Object)null);
    }

    public static <StaticT> Opt<StaticT> of(Object o) {
        return new Opt(o);
    }
}
