package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;

public class LeaderElectionTest {
    static final class NoHealthChecksConsulClientMock extends ConsulClient {

        @Override
        public final Response<Map<String, Check>> getAgentChecks() {
            Map<String, Check> map = new HashMap<>();
            return new Response<Map<String,Check>>(map, 1L, true, 1L);
        }

    }
    @Test
    public void TestLeaderElectionUsingConsul() {
        assertNotNull(new LeaderElection());
        assertEquals(Optional.empty(), LeaderElection.build(new NoHealthChecksConsulClientMock(), "service", "serviceId", 1, () -> "hello").get());
    }
}
