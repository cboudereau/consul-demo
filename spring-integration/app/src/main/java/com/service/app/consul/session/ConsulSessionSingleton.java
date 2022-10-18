package com.service.app.consul.session;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.session.model.NewSession;

final class ConsulSessionSingleton implements ConsulSession {
    static final class DefaultConsulSession {
        private final NewSession newSession = new NewSession();
        
        private String sessionId;
        private List<String> checkIds;
    
        private static Optional<String> renewSession(final ConsulClient cc, final String sessionId) {
            try {
                return Optional.ofNullable(cc.renewSession(sessionId, QueryParams.DEFAULT))
                        .flatMap(x -> Optional.ofNullable(x.getValue()))
                        .flatMap(x -> Optional.ofNullable(x.getId()));
            } catch (OperationException e) {
                if (e.getStatusCode() == 404)
                    return Optional.empty();
                throw e;
            }
        }
    
        public Optional<String> tryGetSessionId(final ConsulClient cc, final String serviceId, final long lockDelayInSecond) {
            if (checkIds == null) {
                List<String> checkIdResult = cc.getAgentChecks().getValue().values().stream()
                        .filter(x -> x.getServiceId().equals(serviceId)).map(x -> x.getCheckId())
                        .collect(Collectors.toList());
                if (checkIdResult.isEmpty()) {
                    ConsulLeaderElection.logger.warn(
                            "{} : empty agent service checks returned, awaiting service and health checks registration",
                            serviceId);
                    return Optional.empty();
                }
                checkIdResult.add("serfHealth");
                checkIds = checkIdResult;
    
                ConsulLeaderElection.logger.debug("{} : setting up session health checks {}", serviceId, checkIds);
                newSession.setLockDelay(lockDelayInSecond);
                newSession.setChecks(checkIds);
            }
    
            sessionId = Optional.ofNullable(sessionId)
                    .flatMap(x -> renewSession(cc, x)).map(sId -> {
                        ConsulLeaderElection.logger.debug("{}/{}: session renewed", serviceId, sId);
                        return sId;
                    })
                    .orElseGet(() -> {
                        String sId = cc.sessionCreate(newSession, QueryParams.DEFAULT).getValue();
                        ConsulLeaderElection.logger.debug("{}/{}: session created", serviceId, sId);
                        return sId;
                    });
    
            return Optional.of(sessionId);
        }
    }    


    private static final DefaultConsulSession defaultConsulSession = new DefaultConsulSession();

    private final ConsulClient cc;

    public ConsulSessionSingleton(final ConsulClient cc) {
        this.cc = cc;
    }
    
    public static synchronized Optional<String> tryGetSessionId(final ConsulClient cc, final String serviceId, final long lockDelayInSecond) {
        return defaultConsulSession.tryGetSessionId(cc, serviceId, lockDelayInSecond);
    }

    @Override
    public Optional<String> tryGetSessionId(final String serviceId, final long lockDelayInSecond) {
        return tryGetSessionId(cc, serviceId, lockDelayInSecond);
    }
}