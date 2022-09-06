package com.service.app.consul.session;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnhandledAwaiter<T> implements Handler<T> {
    private static final Logger logger = LoggerFactory.getLogger(UnhandledAwaiter.class);	

    private final Handler<T> handler;
    private final long timeInSecond;

    public UnhandledAwaiter(long timeInSecond, Handler<T> handler) {
        this.handler = handler;
        this.timeInSecond = timeInSecond;
    }
    @Override
    public Optional<HandlerStatus> apply(T input) {
        Optional<HandlerStatus> status = this.handler.apply(input);
        if (status.map(x -> x == HandlerStatus.UNHANDLED).orElse(false)) {
            try {
                TimeUnit.SECONDS.sleep(this.timeInSecond);
            } catch (InterruptedException e) {
                logger.error("interrupted while awaiting", e);
            }
        }
        return status;
    }
}