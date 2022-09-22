package com.service.app;

public class OrderDto {
    private final Integer orderId;
    public Integer getOrderId() {
        return orderId;
    }

    private final String label;
    
    public String getLabel() {
        return label;
    }

    public OrderDto(final Integer orderId, final String label) {
        this.orderId = orderId;
        this.label = label;
    }
}
