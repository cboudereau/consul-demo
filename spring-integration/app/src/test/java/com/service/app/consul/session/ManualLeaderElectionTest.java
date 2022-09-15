package com.service.app.consul.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ManualLeaderElectionTest {
    
    @Test
    public final void ShouldNotBeActivatedWhenNoConfigurationIsGiven() {
        Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> "hello", null).get();
        assertEquals(Optional.empty(), actual);
    }

    @Test
    public final void ShouldReturnNullWhenNoConfigurationIsGiven() {
        assertNull(ManualLeaderElection.getConfiguration());
    }

    @Test
    public final void ShouldNotBeLeaderWhenConfiguredToFalseIsGiven() {
        Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> "hello", "false").get();
        assertEquals(Optional.of(Optional.empty()), actual);
    }

    @Test
    public final void ShouldBeLeaderWhenConfiguredToTrueIsGiven() {
        String expected = "hello";
        Optional<Optional<String>> actual = new ManualLeaderElection<String>(() -> expected, "true").get();
        assertEquals(Optional.of(Optional.of(expected)), actual);
    }
}
