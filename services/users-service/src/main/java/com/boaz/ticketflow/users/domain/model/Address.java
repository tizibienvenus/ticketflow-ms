package com.boaz.ticketflow.users.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

//@Embeddable
@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends AuditableEntity{
    
    @Column(name = "street_address", nullable = false)
    private String streetAddress;
    
    @Column(name = "address_line_2")
    private String addressLine2;
    
    @Column(nullable = false)
    private String city;
    
    @Column(name = "state_province")
    private String stateProvince;
    
    @Column(name = "postal_code", nullable = false)
    private String postalCode;
    
    @Column(nullable = false)
    private String country;
    
    @Column(name = "country_code", length = 2)
    private String countryCode; // FR, US, etc.
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "address_type")
    private String addressType; // HOME, WORK, HEADQUARTERS, BRANCH, etc.
    
    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "formatted_address")
    private String formattedAddress;

    @Column(name = "place_id")
    private String placeId; // Google Maps Place ID

    // Méthodes utilitaires
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(streetAddress);
        
        if (addressLine2 != null && !addressLine2.isEmpty()) {
            sb.append(", ").append(addressLine2);
        }
        
        sb.append(", ").append(postalCode).append(" ").append(city);
        
        if (stateProvince != null && !stateProvince.isEmpty()) {
            sb.append(", ").append(stateProvince);
        }
        
        sb.append(", ").append(country);
        
        return sb.toString();
    }
    
    public String getShortAddress() {
        return String.format("%s %s, %s", postalCode, city, country);
    }
    
    public boolean isComplete() {
        return streetAddress != null && !streetAddress.isEmpty() &&
               city != null && !city.isEmpty() &&
               postalCode != null && !postalCode.isEmpty() &&
               country != null && !country.isEmpty();
    }
    
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
    
    public void setCoordinates(Double lat, Double lng) {
        this.latitude = lat;
        this.longitude = lng;
    }
    
    public void normalize() {
        if (streetAddress != null) {
            streetAddress = streetAddress.trim();
        }
        if (city != null) {
            city = city.trim().toUpperCase();
        }
        if (stateProvince != null) {
            stateProvince = stateProvince.trim().toUpperCase();
        }
        if (postalCode != null) {
            postalCode = postalCode.trim().toUpperCase();
        }
        if (country != null) {
            country = country.trim().toUpperCase();
        }
        if (countryCode != null) {
            countryCode = countryCode.trim().toUpperCase();
        }
    }
    
    // Méthode pour calculer la distance entre deux adresses (simplifiée)
    public Double calculateDistanceTo(Address other) {
        if (!this.hasCoordinates() || !other.hasCoordinates()) {
            return null;
        }
        
        // Formule de Haversine pour calculer la distance en kilomètres
        final int R = 6371; // Rayon de la Terre en km
        
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    // Méthode pour vérifier si l'adresse est dans un certain rayon
    public boolean isWithinRadius(Address center, Double radiusKm) {
        Double distance = this.calculateDistanceTo(center);
        return distance != null && distance <= radiusKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Address address = (Address) o;
        
        if (!Objects.equals(streetAddress, address.streetAddress)) return false;
        if (!Objects.equals(city, address.city)) return false;
        if (!Objects.equals(postalCode, address.postalCode)) return false;
        return Objects.equals(country, address.country);
    }
    
    @Override
    public int hashCode() {
        int result = streetAddress != null ? streetAddress.hashCode() : 0;
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return getFullAddress();
    }

    @Override
    protected String getPrefix() {
        return "ADDR";
    }
}