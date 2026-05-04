package com.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, name = "full_name", length = 190)
    private String fullName = "";

    @Column(length = 32)
    private String phone = "";

    @Column(length = 500)
    private String address = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    protected User() {}

    public User(String email, String passwordHash, String fullName,
                String phone, String address, Role role, Instant createdAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName == null ? "" : fullName;
        this.phone = phone == null ? "" : phone;
        this.address = address == null ? "" : address;
        this.role = role == null ? Role.USER : role;
        this.createdAt = createdAt;
    }

    public void setRole(Role role) {
        this.role = role == null ? Role.USER : role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? "" : phone;
    }

    public void setAddress(String address) {
        this.address = address == null ? "" : address;
    }

    public Long id() { return id; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public String fullName() { return fullName; }
    public String phone() { return phone == null ? "" : phone; }
    public String address() { return address == null ? "" : address; }
    public Role role() { return role; }
    public boolean enabled() { return enabled; }
    public Instant createdAt() { return createdAt; }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
