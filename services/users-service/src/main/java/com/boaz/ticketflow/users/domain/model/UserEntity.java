package com.boaz.ticketflow.users.domain.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.JOINED)
public class UserEntity extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String identifier;

    @Column(unique = true, nullable = false)
    private String keycloakId;

    @Column(unique = true)
    private String email;
    
    @Column(unique = true)
    @Pattern(regexp = "\\+?[0-9. ()-]{7,25}")
    private String phone;

    private String firstname;
    private String lastname;

    private String profileImage;
    private String coverImage;
    private String fcmToken;

    private String countryCode;
    private String city;
    private String cityId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuthProvider provider = AuthProvider.EMAIL;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(nullable = false, length = 20, name = "user_role")
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>(Set.of(UserRole.USER));

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    /*@OneToMany(
        cascade = CascadeType.ALL, 
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @JoinColumn(name = "user_id")
    @Builder.Default*/
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();

    @Override
    protected String getPrefix() {
        return "USR";
    }

    @Override
    protected boolean includeDateInId() {
        return true; 
    }

    public boolean getEnabled(){
        return this.enabled;
    }
}
