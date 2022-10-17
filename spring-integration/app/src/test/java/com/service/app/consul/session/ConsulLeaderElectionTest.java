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
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;

public class ConsulLeaderElectionTest {
    static final class NoHealthChecksConsulClientMock extends ConsulClient {

        @Override
        public final Response<Map<String, Check>> getAgentChecks() {
            Map<String, Check> map = new HashMap<>();
            return new Response<Map<String,Check>>(map, 1L, true, 1L);
        }

    }

    @Test
    public final void shouldNotProcessWhenNoHealthChecksIsConfigured(){
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new NoHealthChecksConsulClientMock(), "service", "serviceId", 1, () -> "hello").get());
    }

    static class OkConsulClientMock extends ConsulClient {
        private final String serviceId;
        private final boolean isLeader;
        public OkConsulClientMock(String serviceId, boolean isLeader) {
            this.serviceId = serviceId;
            this.isLeader = isLeader;
        }
        @Override
        public final Response<Map<String, Check>> getAgentChecks() {
            HashMap<String, Check> map = new HashMap<>();
            Check value = new Check();
            value.setServiceId(serviceId);
            value.setCheckId("checkId");
            map.put(serviceId, value);

            return new Response<Map<String,Check>>(map, 1L, true, 1L);
        }
        @Override
        public final Response<String> sessionCreate(NewSession newSession, QueryParams queryParams) {
            return new Response<String>("sessionId", 1L, true, 1L);
        }
        @Override
        public final Response<Boolean> setKVValue(String key, String value, PutParams putParams) {
            return new Response<Boolean>(this.isLeader, 1L, true, 1L);
        }
        @Override
        public Response<Session> renewSession(String session, QueryParams queryParams) {
            Session value = new Session();
            value.setId("hello");
            return new Response<Session>(value, 1L, true, 1L);
        }
    }

    @Test
    public final void shouldReturnEmptyWhenConsulAcceptsTheSessionLock() {
        String serviceId = "service-8080";
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new OkConsulClientMock(serviceId, true), "service", serviceId, 1, () -> null).get());
    }

    @Test
    public final void shouldProcessWhenConsulAcceptsTheSessionLock() {
        String serviceId = "service-8080";
        String expected = "hello";
        assertEquals(Optional.of(Optional.of(expected)), new ConsulLeaderElection<>(new OkConsulClientMock(serviceId, true), "service", serviceId, 1, () -> expected).get());
    }

    @Test
    public final void shouldNotProcessWhenConsulRefusesTheSessionLock() {
        String serviceId = "service-8080";
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new OkConsulClientMock(serviceId, false), "service", serviceId, 1, () -> "hello").get());
    }

    @Test
    public final void shouldProcessWhenConsulRenewsTheSessionLock() {
        String serviceId = "service-8080";
        String expected = "hello";
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(new OkConsulClientMock(serviceId, true), "service", serviceId, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
    }

    static final class KoNotReadyConsulClientMock extends ConsulClient {
        @Override
        public Response<Map<String, Check>> getAgentChecks() {
            throw new ConsulException("not ready");
        }
    }

    @Test
    public final void shouldNotProcessWhenConsulFails() {
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new KoNotReadyConsulClientMock(), "service", "serviceId", 1, () -> "hello").get());
    }

    static final class KoSessionClientMock extends OkConsulClientMock {
        private int statusCode;

        public KoSessionClientMock(String serviceId, boolean isLeader, int statusCode) {
            super(serviceId, isLeader);
            this.statusCode = statusCode;
        }

        @Override
        public final Response<Session> renewSession(String session, QueryParams queryParams) {
            throw new OperationException(statusCode, "", "");
        }
    }

    @Test
    public final void shouldProcessWhenConsulSessionIsRecreatedFails() {
        String expected = "hello";
        String serviceId = "serviceId";
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(new KoSessionClientMock(serviceId, true, 404), "service", serviceId, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
    }

    @Test
    public final void shouldNotProcessWhenConsulSessionIsRenewFails() {
        String expected = "hello";
        String serviceId = "serviceId";
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(new KoSessionClientMock(serviceId, true, 500), "service", serviceId, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.empty()), consulLeaderElection.get());
    }
}
