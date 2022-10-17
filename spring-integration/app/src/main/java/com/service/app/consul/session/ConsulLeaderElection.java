package com.service.app.consul.session;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;

final class ConsulLeaderElection<T> implements Handler<T> {

    static final class ConsulSession {
        private final NewSession newSession = new NewSession();
        private final ConsulClient cc;
        private final String serviceId;

        private String sessionId = null;
        private List<String> checkIds = null;

        public ConsulSession(final ConsulClient cc, final String serviceId, final long lockDelayInSecond) {
            this.cc = cc;
            this.serviceId = serviceId;
            this.newSession.setLockDelay(lockDelayInSecond);
        }

        private static Optional<String> renewSession(final ConsulClient cc, final String sessionId) {
            try {
                return Optional.ofNullable(cc.renewSession(sessionId, QueryParams.DEFAULT))
                    .flatMap(x -> Optional.ofNullable(x.getValue()))
                    .flatMap(x -> Optional.ofNullable(x.getId()));
            } catch (OperationException e) {
                if (e.getStatusCode() == 404) return Optional.empty();
                throw e;
            }
        }
        
        private Optional<String> tryGetSessionId() {
            if (checkIds == null) {
                List<String> checkIdResult = cc.getAgentChecks().getValue().values().stream().filter(x -> x.getServiceId().equals(serviceId)).map(x -> x.getCheckId()).collect(Collectors.toList());
                if (checkIdResult.isEmpty()) {
                    logger.warn("{} : empty agent service checks returned, awaiting service and health checks registration", serviceId);
                    return Optional.empty();
                }
                checkIdResult.add("serfHealth");
                checkIds = checkIdResult;
                
                logger.debug("{} : setting up session health checks {}", serviceId, checkIds);
                newSession.setChecks(checkIds);
            }
            
            sessionId = Optional.ofNullable(sessionId)
                .flatMap(x -> renewSession(cc, x)).map(sId -> {
                    logger.debug("{}/{}: session renewed", serviceId, sId);
                    return sId;
                })
                .orElseGet(() -> {
                    String sId = cc.sessionCreate(newSession, QueryParams.DEFAULT).getValue();
                    logger.debug("{}/{}: session created", serviceId, sId);
                    return sId;
                });
    
            return Optional.of(sessionId);
        }    
    }

    private static final Logger logger = LoggerFactory.getLogger(ConsulLeaderElection.class);

    private final ConsulClient cc;
    private final ConsulSession consulSession;
    private final String serviceId;
    private final String leaderKey;
    private final Supplier<T> supplier;

    public ConsulLeaderElection(final ConsulClient cc, final String service, final String serviceId, final long lockDelayInSecond, final Supplier<T> handler) {
        this.cc = cc;
        this.serviceId = serviceId;
        this.supplier = handler;
        this.consulSession = new ConsulSession(cc, serviceId, lockDelayInSecond);
        this.leaderKey = String.format("%s/leader", service);
    }

    @Override
    public Optional<Optional<T>> get() {
        logger.debug("{} : starting leader election consul based", serviceId);
        try {
            Optional<String> sessionIdO = consulSession.tryGetSessionId();

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
        } 
        catch (ConsulException e) {
            logger.error("{}: error while managing leader", serviceId, e);
            return Optional.of(Optional.empty());
        }
    }
}