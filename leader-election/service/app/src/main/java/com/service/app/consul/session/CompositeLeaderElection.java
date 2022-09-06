package com.service.app.consul.session;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompositeLeaderElection<T> implements Consumer<T> {
    private final List<Handler<T>> handlers;
    private static final Logger logger = LoggerFactory.getLogger(CompositeLeaderElection.class);

    public CompositeLeaderElection(List<Handler<T>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void accept(T input) {
        if(this.handlers.stream().map(x -> x.apply(input)).filter(x -> x.isPresent()).findFirst().orElse(Optional.empty()).isPresent()){
            return;
        }
        logger.warn("leader election implementation not found");
    }
}