package com.boaz.ticketflow.common.utils;

public record Location (double latitude, double longitude) {
    public Location {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    public double distanceTo(Location other) {
        // Haversine formula (simplified for brevity, use proper implementation)
        double R = 6371e3; // metres
        double φ1 = Math.toRadians(latitude);
        double φ2 = Math.toRadians(other.latitude);
        double Δφ = Math.toRadians(other.latitude - latitude);
        double Δλ = Math.toRadians(other.longitude - longitude);

        double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }
}