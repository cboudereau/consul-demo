package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ManualLeaderElectionTest {
    
    @Test
    public final void ShouldNotBeActivatedWhenNoConfigurationIsGiven() {
        final Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> "hello", null).get();
        assertEquals(Optional.empty(), actual);
    }

    @Test
    public final void ShouldReturnNullWhenNoConfigurationIsGiven() {
        assertNull(ManualLeaderElection.getConfiguration());
    }

    @Test
    public final void ShouldNotBeLeaderWhenConfiguredToFalseIsGiven() {
        final Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> "hello", "false").get();
        assertEquals(Optional.of(Optional.empty()), actual);
    }

    @Test
    public final void ShouldBeLeaderWhenConfiguredToTrueIsGiven() {
        final String expected = "hello";
        final Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> expected, "true").get();
        assertEquals(Optional.of(Optional.of(expected)), actual);
    }

    @Test
    public final void shouldReturnEmptyWhenConfiguredToTrueIsGiven() {
        final Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> null, "true").get();
        assertEquals(Optional.of(Optional.empty()), actual);
    }
}
