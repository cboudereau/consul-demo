package com.service.app.consul.session;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface Handler<T> extends Function<T, Optional<HandlerStatus>> { }