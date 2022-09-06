package com.service.app.consul.session;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManualLeaderElection<T> implements Handler<T> {
    private final Consumer<T> action;
    private static final Logger logger = LoggerFactory.getLogger(ManualLeaderElection.class);
    public ManualLeaderElection(Consumer<T> action) {
        this.action = action;
    }

    @Override
    public Optional<HandlerStatus> apply(T input) {
        Optional<String> isLeaderForced = Optional.ofNullable(System.getenv("IS_LEADER"));
                
        if (!isLeaderForced.isPresent()) {
            return Optional.empty();
        }
        
        logger.warn("forced leader: enabled");
        if(isLeaderForced.map(x -> x.equalsIgnoreCase("true")).orElse(false)){
            logger.warn("forced leader: leader has been elected");
            this.action.accept(input);
            return Optional.of(HandlerStatus.HANDLED);
        }
        logger.warn("forced leader: service has not been elected");
        return Optional.of(HandlerStatus.UNHANDLED);
    }
}