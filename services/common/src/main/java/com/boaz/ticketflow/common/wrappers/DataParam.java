package com.boaz.ticketflow.common.wrappers;

import java.util.List;

public record DataParam <T>(
    List<T> data
) {
    
}