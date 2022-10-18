package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CompositeLeaderElectionTest {
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
        final Optional<String> expected = Optional.of("hello");
        final Handler<String> candidate = () -> Optional.of(expected);
        final Handler<String> shouldFail = () -> { throw new RuntimeException(); }; 
        assertEquals(expected, new CompositeLeaderElection<>(Arrays.asList(candidate, shouldFail)).get());
    }

}
