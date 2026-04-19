package com.boaz.ticketflow.common.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DriverRideRejectedEvent extends DomainEvent {
    private final String rideId;
    private final String driverId;
}