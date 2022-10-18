package com.service.app.consul.session;

import java.util.HashMap;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;

class OkConsulClientMock extends ConsulClient {
    private final String serviceId;
    private final boolean isLeader;
    public OkConsulClientMock(final String serviceId, final boolean isLeader) {
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