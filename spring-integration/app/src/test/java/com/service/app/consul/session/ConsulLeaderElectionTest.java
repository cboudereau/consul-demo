package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.session.model.Session;
import com.service.app.consul.session.ConsulSessionSingleton.DefaultConsulSession;

public class ConsulLeaderElectionTest {
    static final class TrueConsulSession implements ConsulSession {
        private final ConsulClient cc;
        private final DefaultConsulSession defaultConsulSession = new DefaultConsulSession();
        public TrueConsulSession(final ConsulClient cc) {
            this.cc = cc;
        }

        @Override
        public final Optional<String> tryGetSessionId(String serviceId, long lockDelayInSecond) {
            return defaultConsulSession.tryGetSessionId(cc, serviceId, lockDelayInSecond);
        }
    }

    static final class NoHealthChecksConsulClientMock extends ConsulClient {

        @Override
        public final Response<Map<String, Check>> getAgentChecks() {
            Map<String, Check> map = new HashMap<>();
            return new Response<Map<String,Check>>(map, 1L, true, 1L);
        }

    }

    @Test
    public final void shouldNotProcessWhenNoHealthChecksIsConfigured(){
        final ConsulClient cc = new NoHealthChecksConsulClientMock();
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", "serviceId", 1, () -> "hello").get());
    }

    @Test
    public final void shouldReturnEmptyWhenConsulAcceptsTheSessionLock() {
        final String serviceId = "service-8080";
        final ConsulClient cc = new OkConsulClientMock(serviceId, true);
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", serviceId, 1, () -> null).get());
    }

    @Test
    public final void shouldProcessWhenConsulAcceptsTheSessionLock() {
        final String serviceId = "service-8080";
        final String expected = "hello";
        final ConsulClient cc = new OkConsulClientMock(serviceId, true);
        assertEquals(Optional.of(Optional.of(expected)), new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", serviceId, 1, () -> expected).get());
    }

    @Test
    public final void shouldNotProcessWhenConsulRefusesTheSessionLock() {
        final String serviceId = "service-8080";
        final ConsulClient cc = new OkConsulClientMock(serviceId, false);
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", serviceId, 1, () -> "hello").get());
    }

    @Test
    public final void shouldProcessWhenConsulRenewsTheSessionLock() {
        final String serviceId = "service-8080";
        final String expected = "hello";
        final ConsulClient cc = new OkConsulClientMock(serviceId, true);
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", serviceId, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
    }

    static final class KoNotReadyConsulClientMock extends ConsulClient {
        @Override
        public final Response<Map<String, Check>> getAgentChecks() {
            throw new ConsulException("not ready");
        }
    }

    @Test
    public final void shouldNotProcessWhenConsulFails() {
        final ConsulClient cc = new KoNotReadyConsulClientMock();
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", "serviceId", 1, () -> "hello").get());
    }

    static final class KoSessionClientMock extends OkConsulClientMock {
        private final int statusCode;

        public KoSessionClientMock(final String serviceId, final boolean isLeader, final int statusCode) {
            super(serviceId, isLeader);
            this.statusCode = statusCode;
        }

        @Override
        public final Response<Session> renewSession(final String session, final QueryParams queryParams) {
            throw new OperationException(statusCode, "", "");
        }
    }

    @Test
    public final void shouldProcessWhenConsulSessionIsRecreatedFails() {
        final String expected = "hello";
        final String serviceId = "serviceId";
        final ConsulClient cc = new KoSessionClientMock(serviceId, true, 404);
        final ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", serviceId, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
    }

    @Test
    public final void shouldNotProcessWhenConsulSessionIsRenewFails() {
        final String expected = "hello";
        final String serviceId = "serviceId";
        final ConsulClient cc = new KoSessionClientMock(serviceId, true, 500);
        final ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(cc, new TrueConsulSession(cc), "service", serviceId, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.empty()), consulLeaderElection.get());
    }
}
