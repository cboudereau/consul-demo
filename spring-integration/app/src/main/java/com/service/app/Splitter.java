package com.service.app;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service("Splitter")
public class Splitter extends AbstractMessageSplitter{

    private static final Logger logger = LoggerFactory.getLogger(Splitter.class);

    @Override
    @SuppressWarnings("unchecked") // this is already done in the existing codebase
    protected Object splitMessage(Message<?> message) {
        final List<OrderDto> orders = (List<OrderDto>) message.getPayload();
        orders.forEach(order -> logger.info("Splitter: {}/{}", order.getLabel(), order.getOrderId()));
        return orders;
    }
    
}
