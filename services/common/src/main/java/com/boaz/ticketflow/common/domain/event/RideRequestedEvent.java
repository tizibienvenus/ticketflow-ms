package com.boaz.ticketflow.common.domain.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class RideRequestedEvent extends DomainEvent {
    
    private final String rideId;
    private final String passengerId;
    private final double pickupLat;
    private final double pickupLon;
    private final double destinationLat;
    private final double destinationLon;
    private final String rideType;
    private final String city;
    private final Instant timestamp;

}