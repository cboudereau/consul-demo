package com.service.app.consul.session;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import com.ecwid.consul.v1.ConsulClient;

public final class LeaderElection {
    public static final <T> Supplier<Optional<T>> build(final ConsulClient consulClient, final String service, final String serviceId, final int extraDelayInSecond, final Supplier<T> supplier) {
        Handler<T> manualLeader = new ManualLeaderElection<>(supplier, ManualLeaderElection.getConfiguration());
        Handler<T> consulLeader = new ConsulLeaderElection<>(consulClient, new ConsulSessionSingleton(consulClient), service, serviceId, extraDelayInSecond, supplier);
        return new CompositeLeaderElection<>(Arrays.asList(manualLeader, consulLeader));
    }
}
