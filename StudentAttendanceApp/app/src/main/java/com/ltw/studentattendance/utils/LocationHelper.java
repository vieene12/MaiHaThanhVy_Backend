package com.ltw.studentattendance.utils;

public class LocationHelper {
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * Calculates the distance between two coordinates in meters using the Haversine formula.
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(rLat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Checks if current position is within allowed distance (default 100 meters).
     */
    public static boolean isWithinRadius(double currentLat, double currentLon,
                                         double targetLat, double targetLon,
                                         double maxDistanceMeters) {
        double distance = calculateDistance(currentLat, currentLon, targetLat, targetLon);
        return distance <= maxDistanceMeters;
    }
}
