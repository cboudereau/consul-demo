/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helloworld.service;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for exporting metrics to
 * OTLP.
 *
 * @author Eddú Meléndez
 * @since 3.0.0
 */
@AutoConfiguration(before = { CompositeMeterRegistryAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class }, after = MetricsAutoConfiguration.class)
@ConditionalOnBean(Clock.class)
@ConditionalOnClass(OtlpMeterRegistry.class)
@ConditionalOnEnabledMetricsExport("otlp")
@EnableConfigurationProperties(OtlpProperties.class)
public class OtlpMetricsExportAutoConfiguration {

    private final OtlpProperties properties;

    public OtlpMetricsExportAutoConfiguration(OtlpProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public OtlpConfig otlpConfig() {
        return new OtlpPropertiesConfigAdapter(this.properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OtlpMeterRegistry otlpMeterRegistry(OtlpConfig otlpConfig, Clock clock) {
        return new OtlpMeterRegistry(otlpConfig, clock);
    }

}