package com.service.app.consul.session;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompositeLeaderElection<T> implements Supplier<Optional<T>> {
    private final List<Handler<T>> handlers;
    private static final Logger logger = LoggerFactory.getLogger(CompositeLeaderElection.class);

    public CompositeLeaderElection(final List<Handler<T>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public final Optional<T> get() {
        Optional<Optional<T>> result = this.handlers.stream().map(x -> x.get()).filter(x -> x.isPresent()).findFirst().orElse(Optional.empty());
        if(result.isEmpty()){
            logger.warn("leader election implementation not found");
            return Optional.empty();
        }
        return result.get();
    }
}