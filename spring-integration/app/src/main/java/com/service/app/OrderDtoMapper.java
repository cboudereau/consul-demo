package com.service.app;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapper implements RowMapper<OrderDto> {

    @Override
    public OrderDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderDto order = new OrderDto(rs.getInt("order_id"), rs.getString("label"));
        return order;
    }
    
}
