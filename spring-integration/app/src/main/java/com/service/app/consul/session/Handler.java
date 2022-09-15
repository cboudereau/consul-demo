package com.service.app.consul.session;

import java.util.Optional;
import java.util.function.Supplier;

@FunctionalInterface
interface Handler<T> extends Supplier<Optional<Optional<T>>> { }