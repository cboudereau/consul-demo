package com.service.app.consul.session;

import java.util.Optional;

interface ConsulSession {
    Optional<String> tryGetSessionId(final String serviceId, final long lockDelayInSecond);
}