package com.service.app;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service("Consumer")
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public void handle(Message<OrderDto> message) {
        OrderDto order = message.getPayload();
        logger.info("Consumer:{}/{}", order.getLabel(), order.getOrderId());
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            logger.warn("interrupted", e);
        }
    } 
}
