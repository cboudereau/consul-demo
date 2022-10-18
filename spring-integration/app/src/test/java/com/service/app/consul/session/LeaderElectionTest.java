package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.session.model.NewSession;

public class LeaderElectionTest {
    static final class NoHealthChecksConsulClientMock extends ConsulClient {

        @Override
        public final Response<Map<String, Check>> getAgentChecks() {
            final Map<String, Check> map = new HashMap<>();
            return new Response<Map<String, Check>>(map, 1L, true, 1L);
        }

    }

    @Test
    public void TestLeaderElectionUsingConsul() {
        assertNotNull(new LeaderElection());
        assertEquals(Optional.empty(), LeaderElection
                .build(new NoHealthChecksConsulClientMock(), "service", "serviceId", 1, () -> "hello").get());
    }

    static final class KoSessionCreateConsulClient extends OkConsulClientMock {

        public KoSessionCreateConsulClient(String serviceId, boolean isLeader) {
            super(serviceId, isLeader);
        }

        @Override
        public Response<String> sessionCreate(NewSession newSession, QueryParams queryParams, String token) {
            throw new ConsulException("cannot create session");
        }

    }

    @Test
    public void TestLeaderElectionUsingSingletonSession() {
        final String serviceId = "serviceId";
        final String expected = "hello";
        assertEquals(Optional.of(expected), LeaderElection
                .build(new OkConsulClientMock(serviceId, true), "service", serviceId, 1, () -> expected).get());

        assertEquals(Optional.of(expected), LeaderElection
                .build(new KoSessionCreateConsulClient(serviceId, true), "service", serviceId, 1, () -> expected)
                .get());
    }
}
