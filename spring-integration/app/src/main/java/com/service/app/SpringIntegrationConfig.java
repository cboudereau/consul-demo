package com.service.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.ecwid.consul.v1.ConsulClient;
import com.service.app.consul.session.LeaderElection;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.Message;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import java.util.Optional;
import java.util.function.Supplier;

@Configuration
@DependsOn("dbConfig")
@ImportResource("flow-config.xml")
public class SpringIntegrationConfig {
    @Autowired Environment env;
    public final class ConsulSourcePollingChannelAdapter extends SourcePollingChannelAdapter {
        private static final Logger logger = LoggerFactory.getLogger(ConsulSourcePollingChannelAdapter.class);
        
        private String service = env.getProperty("spring.application.name");
        private String serviceId = env.getProperty("spring.cloud.consul.discovery.instance-id");

        private final Supplier<Optional<Message<?>>> supplier;

        public ConsulSourcePollingChannelAdapter(ConsulClient consulClient, int extraDelayInSecond) {
            logger.info("starting {}/{}", service, serviceId);
            this.supplier = LeaderElection.build(consulClient, service, serviceId, extraDelayInSecond, () -> super.receiveMessage());
        }

        @Override
        protected Message<?> receiveMessage() {
            Message<?> result = supplier.get().orElse(null);
            return result;
        }
    }

    @Inject
    private ConfigurableListableBeanFactory beanFactory;

    @Bean("jdbcPollingAdapter")
    @DependsOn("jdbcTemplate")
    public JdbcPollingChannelAdapter jdbcPollingAdapter(final DataSource dataSource, final RowMapper<OrderDto> rowMapper) {
        JdbcPollingChannelAdapter bean = new JdbcPollingChannelAdapter(dataSource, "select order_id, label from orders");
        bean.setRowMapper(rowMapper);
        return bean;
    }

    @Bean("inboundChannel")
    @DependsOn("jdbcPollingAdapter")
    public ConsulSourcePollingChannelAdapter inboundChannel(final @Named("jdbcPollingAdapter") JdbcPollingChannelAdapter jdbcPollingAdapters) throws IllegalAccessException {
        Trigger poller = new PeriodicTrigger(5000);
        
        ConsulSourcePollingChannelAdapter bean = new ConsulSourcePollingChannelAdapter(new ConsulClient(), 10);
        bean.setBeanFactory(beanFactory);
        bean.setOutputChannelName("input");
        bean.setSource(jdbcPollingAdapters);
        bean.setTrigger(poller);
        return bean;
    }
}
