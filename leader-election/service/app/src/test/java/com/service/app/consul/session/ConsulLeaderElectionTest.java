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
        public Response<Map<String, Check>> getAgentChecks() {
            Map<String, Check> map = new HashMap<>();
            return new Response<Map<String,Check>>(map, 1L, true, 1L);
        }

    }

    @Test
    public void ShouldNotProcessWhenNoHealthChecksIsConfigured(){
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new NoHealthChecksConsulClientMock(), "service", 1, () -> "hello").get());
    }

    static class OkConsulClientMock extends ConsulClient {
        private final String service;
        private final boolean isLeader;
        public OkConsulClientMock(String service, boolean isLeader) {
            this.service = service;
            this.isLeader = isLeader;
        }
        @Override
        public Response<Map<String, Check>> getAgentChecks() {
            HashMap<String, Check> map = new HashMap<>();
            Check value = new Check();
            value.setServiceName(service);
            value.setCheckId("checkId");
            map.put(service, value);

            return new Response<Map<String,Check>>(map, 1L, true, 1L);
        }
        @Override
        public Response<String> sessionCreate(NewSession newSession, QueryParams queryParams) {
            return new Response<String>("sessionId", 1L, true, 1L);
        }
        @Override
        public Response<Boolean> setKVValue(String key, String value, PutParams putParams) {
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
    public void ShouldProcessWhenConsulAcceptsTheSessionLock() {
        String service = "service";
        String expected = "hello";
        assertEquals(Optional.of(Optional.of(expected)), new ConsulLeaderElection<>(new OkConsulClientMock(service, true), service, 1, () -> expected).get());
    }

    @Test
    public void ShouldNotProcessWhenConsulRefusesTheSessionLock() {
        String service = "service";
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new OkConsulClientMock(service, false), service, 1, () -> "hello").get());
    }

    @Test
    public void ShouldProcessWhenConsulRenewsTheSessionLock() {
        String service = "service";
        String expected = "hello";
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(new OkConsulClientMock(service, true), service, 1, () -> expected);
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
    public void ShouldNotProcessWhenConsulFails() {
        assertEquals(Optional.of(Optional.empty()), new ConsulLeaderElection<>(new KoNotReadyConsulClientMock(), "service", 1, () -> "hello").get());
    }

    class KoSessionClientMock extends OkConsulClientMock {
        private int statusCode;

        public KoSessionClientMock(String service, boolean isLeader, int statusCode) {
            super(service, isLeader);
            this.statusCode = statusCode;
        }

        @Override
        public Response<Session> renewSession(String session, QueryParams queryParams) {
            throw new OperationException(statusCode, "", "");
        }
    }

    @Test
    public void ShouldProcessWhenConsulSessionIsRecreatedFails() {
        String expected = "hello";
        String service = "service";
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(new KoSessionClientMock(service, true, 404), service, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
    }

    @Test
    public void ShouldNotProcessWhenConsulSessionIsRenewFails() {
        String expected = "hello";
        String service = "service";
        ConsulLeaderElection<String> consulLeaderElection = new ConsulLeaderElection<>(new KoSessionClientMock(service, true, 500), service, 1, () -> expected);
        assertEquals(Optional.of(Optional.of(expected)), consulLeaderElection.get());
        assertEquals(Optional.of(Optional.empty()), consulLeaderElection.get());
    }
}
