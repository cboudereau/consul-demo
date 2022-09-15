package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public final class CompositeLeaderElectionTest {
    @Test
    public final void shouldReturnEmptyWhenNoHandlerIsGiven() {
        assertEquals(Optional.empty(), new CompositeLeaderElection<>(Collections.emptyList()).get());
    }

    @Test
    public final void shouldReturnEmptyWhenNoAvailableHandlerIsGiven() {
        assertEquals(Optional.empty(), new CompositeLeaderElection<>(Arrays.asList(() -> Optional.empty())).get());
    }

    @Test
    public final void shouldReturnTheFirstResultWhenMultipleAvailableHandlersIsGiven() {
        Optional<String> expected = Optional.of("hello");
        Handler<String> candidate = () -> Optional.of(expected);
        Handler<String> shouldFail = () -> { throw new RuntimeException(); }; 
        assertEquals(expected, new CompositeLeaderElection<>(Arrays.asList(candidate, shouldFail)).get());
    }

}
