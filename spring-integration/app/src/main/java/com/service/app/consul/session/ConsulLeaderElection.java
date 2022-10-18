package com.service.app.consul.session;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.PutParams;

final class ConsulLeaderElection<T> implements Handler<T> {

    static final Logger logger = LoggerFactory.getLogger(ConsulLeaderElection.class);

    private final ConsulClient cc;
    private final String serviceId;
    private final String leaderKey;
    private final Supplier<T> supplier;
    private final long lockDelayInSecond;
    private final ConsulSession consulSession;

    public ConsulLeaderElection(final ConsulClient cc, final ConsulSession consulSession, final String service,
            final String serviceId, final long lockDelayInSecond, final Supplier<T> handler) {
        this.cc = cc;
        this.consulSession = consulSession;
        this.serviceId = serviceId;
        this.lockDelayInSecond = lockDelayInSecond;
        this.supplier = handler;
        this.leaderKey = String.format("%s/leader", service);
    }

    @Override
    public Optional<Optional<T>> get() {
        logger.debug("{} : starting leader election consul based", serviceId);
        try {
            Optional<String> sessionIdO = consulSession.tryGetSessionId(serviceId, lockDelayInSecond);

            if (sessionIdO.isEmpty()) {
                return Optional.of(Optional.empty());
            } else {
                String sessionId = sessionIdO.get();
                PutParams acquireSessionParams = new PutParams();
                acquireSessionParams.setAcquireSession(sessionId);
                if (cc.setKVValue(leaderKey, sessionId, acquireSessionParams).getValue()) {
                    logger.debug("{}: leader", serviceId);
                    return Optional.of(Optional.ofNullable(this.supplier.get()));
                }
                logger.debug("{}: not leader", serviceId);
                return Optional.of(Optional.empty());
            }
        } catch (ConsulException e) {
            logger.error("{}: error while managing leader", serviceId, e);
            return Optional.of(Optional.empty());
        }
    }
}