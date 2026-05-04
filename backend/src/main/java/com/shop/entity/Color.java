package com.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Color {

    @Column(name = "color_name", length = 64)
    private String name;

    @Column(name = "color_hex", length = 16)
    private String hex;

    protected Color() {}

    public Color(String name, String hex) {
        this.name = name;
        this.hex = hex;
    }

    public String name() { return name; }
    public String hex() { return hex; }

    public void setName(String name) { this.name = name; }
    public void setHex(String hex) { this.hex = hex; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color other)) return false;
        return Objects.equals(name, other.name) && Objects.equals(hex, other.hex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hex);
    }
}
