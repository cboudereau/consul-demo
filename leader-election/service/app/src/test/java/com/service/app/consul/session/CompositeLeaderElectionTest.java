package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CompositeLeaderElectionTest {
    @Test
    public void ShouldReturnEmptyWhenNoHandlerIsGiven() {
        assertEquals(Optional.empty(), new CompositeLeaderElection<>(Collections.emptyList()).get());
    }

    @Test
    public void ShouldReturnEmptyWhenNoAvailableHandlerIsGiven() {
        assertEquals(Optional.empty(), new CompositeLeaderElection<>(Arrays.asList(() -> Optional.empty())).get());
    }

    @Test
    public void ShouldReturnTheFirstResultWhenMultipleAvailableHandlersIsGiven() {
        Optional<String> expected = Optional.of("hello");
        Handler<String> candidate = () -> Optional.of(expected);
        Handler<String> shouldFail = () -> { throw new RuntimeException(); }; 
        assertEquals(expected, new CompositeLeaderElection<>(Arrays.asList(candidate, shouldFail)).get());
    }

}
