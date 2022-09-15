package com.service.app.consul.session;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ManualLeaderElection<T> implements Handler<T> {
    private final Supplier<T> supplier;
    private static final Logger logger = LoggerFactory.getLogger(ManualLeaderElection.class);
    public ManualLeaderElection(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Optional<Optional<T>> get() {
        Optional<String> isLeaderForced = Optional.ofNullable(System.getenv("IS_LEADER"));
                
        if (!isLeaderForced.isPresent()) {
            return Optional.empty();
        }
        
        logger.warn("forced leader: enabled");
        if(isLeaderForced.map(x -> x.equalsIgnoreCase("true")).orElse(false)){
            logger.warn("forced leader: leader has been elected");
            return Optional.of(Optional.of(this.supplier.get()));
        }
        logger.warn("forced leader: service has not been elected");
        return Optional.of(Optional.empty());
    }
}