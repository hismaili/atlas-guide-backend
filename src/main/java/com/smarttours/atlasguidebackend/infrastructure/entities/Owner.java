package com.smarttours.atlasguidebackend.infrastructure.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "itinerary_owners")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_plan_seq")
    @SequenceGenerator(name = "day_plan_seq", sequenceName = "day_plan_seq", allocationSize = 50)
    private Long id;

    private String name;

    private String email;

    private String ipAddress;

    public Owner() {
    }

    public Owner(String name, String email, String ipAddress) {
        this.name = name;
        this.email = email;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Owner owner = (Owner) o;
        return Objects.equals(id, owner.id) && Objects.equals(name, owner.name) && Objects.equals(email, owner.email) && Objects.equals(ipAddress, owner.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, ipAddress);
    }
}
