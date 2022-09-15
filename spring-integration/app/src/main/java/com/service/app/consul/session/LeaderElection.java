package com.service.app.consul.session;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import com.ecwid.consul.v1.ConsulClient;

public final class LeaderElection {
    public static final <T> Supplier<Optional<T>> build(ConsulClient consulClient, String service, int extraDelayInSecond, Supplier<T> supplier) {
        Handler<T> manualLeader = new ManualLeaderElection<>(supplier);
        Handler<T> consulLeader = new ConsulLeaderElection<>(consulClient, service, extraDelayInSecond, supplier);
        return new CompositeLeaderElection<>(Arrays.asList(manualLeader, consulLeader));
    }
}
