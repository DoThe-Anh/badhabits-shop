package com.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Slug không được rỗng")
    @Size(max = 100, message = "Slug tối đa 100 ký tự")
    @Pattern(regexp = "[a-z0-9-]+",
             message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @NotBlank(message = "Tên không được rỗng")
    @Size(max = 200, message = "Tên tối đa 200 ký tự")
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    @Column(length = 1000)
    private String description;

    public Category() {}

    public Category(String slug, String name, String description) {
        this.slug = slug;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public String slug() { return slug; }
    public String name() { return name; }
    public String description() { return description; }

    public void setSlug(String slug) { this.slug = slug; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
