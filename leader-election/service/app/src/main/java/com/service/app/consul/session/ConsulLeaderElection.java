package com.service.app.consul.session;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;

public final class ConsulLeaderElection<T> implements Handler<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConsulLeaderElection.class);

    private final ConsulClient cc;
    private final String service;
    private final String leaderKey;
    private final Handler<T> handler;
    private final NewSession newSession;
    
    private String sessionId = null;
    private List<String> checkIds = null;
    
    public ConsulLeaderElection(ConsulClient cc, String service, long lockDelayInSecond, Handler<T> handler) {
        this.newSession = new NewSession();
        this.newSession.setLockDelay(lockDelayInSecond);
        this.cc = cc;
        this.service = service;
        this.handler = handler;
        this.leaderKey = String.format("%s/leader", service);
    }

    private static Optional<String> renewSession(ConsulClient cc, String sessionId) {
        try {
            return Optional.ofNullable(cc.renewSession(sessionId, QueryParams.DEFAULT))
                .flatMap(x -> Optional.ofNullable(x.getValue()))
                .flatMap(x -> Optional.ofNullable(x.getId()));
        } catch (OperationException e) {
            if (e.getStatusCode() == 404) return Optional.empty();
            throw e;
        }
    }
    
    @Override
    public Optional<HandlerStatus> apply(T input) {
        logger.info("starting leader election consul based");
        try {
            if (checkIds == null) {
                List<String> checkIdResult = cc.getAgentChecks().getValue().values().stream().filter(x -> x.getServiceName().equals(service)).map(x -> x.getCheckId()).collect(Collectors.toList());
                if (checkIdResult.isEmpty()) {
                    logger.warn("empty {} agent service checks returned, awaiting service and health checks registration", service);
                    return Optional.empty();
                }
                checkIdResult.add("serfHealth");
                checkIds = checkIdResult;
                
                logger.info("setting up session health checks {}", checkIds);
                newSession.setChecks(checkIds);
            }
            
            sessionId = Optional.ofNullable(sessionId)
                .flatMap(x -> renewSession(cc, x)).map(sId -> {
                    logger.info("{}: session renewed", sId);
                    return sId;
                })
                .orElseGet(() -> {
                    String sId = cc.sessionCreate(newSession, QueryParams.DEFAULT).getValue();
                    logger.info("{}: session created", sId);
                    return sId;
                });

            PutParams acquireSessionParams = new PutParams();
            acquireSessionParams.setAcquireSession(sessionId);
            if (cc.setKVValue(leaderKey, sessionId, acquireSessionParams).getValue()) {
                logger.info("{}: leader", service);
                return this.handler.apply(input);
            } 
            logger.info("{}: not leader", service);
            return Optional.of(HandlerStatus.UNHANDLED);
        } 
        catch (ConsulException e) {
            logger.error("error while managing leader", e);
            return Optional.of(HandlerStatus.UNHANDLED);
        }
    }
}