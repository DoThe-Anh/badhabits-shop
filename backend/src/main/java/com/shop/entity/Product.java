package com.shop.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Slug không được rỗng")
    @Size(max = 200, message = "Slug tối đa 200 ký tự")
    @Pattern(regexp = "[a-z0-9-]+",
             message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    @Column(unique = true, nullable = false, length = 200)
    private String slug;

    @NotBlank(message = "Tên sản phẩm không được rỗng")
    @Size(max = 500, message = "Tên tối đa 500 ký tự")
    @Column(nullable = false, length = 500)
    private String name;

    @NotNull(message = "Phải chọn danh mục")
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 100, message = "Collection tối đa 100 ký tự")
    @Column(length = 100)
    private String collection;

    @PositiveOrZero(message = "Giá phải >= 0")
    @Column(nullable = false)
    private long price;

    @PositiveOrZero(message = "Giá gốc phải >= 0")
    @Column(name = "original_price")
    private Long originalPrice;

    @NotBlank(message = "Phải có ảnh chính (upload file hoặc dán URL)")
    @Size(max = 500)
    @Column(name = "primary_image", nullable = false, length = 500)
    private String primaryImage;

    @Size(max = 500)
    @Column(name = "hover_image", length = 500)
    private String hoverImage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_gallery", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 500)
    private List<String> gallery = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size_value", length = 32)
    private List<String> sizes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"))
    private List<Color> colors = new ArrayList<>();

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    @Column(length = 2000)
    private String description;

    @Size(max = 1000, message = "Chất liệu tối đa 1000 ký tự")
    @Column(length = 1000)
    private String material;

    @Min(value = 0, message = "Tồn kho phải >= 0")
    @Column(nullable = false)
    private int stock;

    @Column(name = "is_new", nullable = false)
    private boolean isNew;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;

    public Product() {}

    public Product(String slug, String name, Category category, String collection,
                   long price, Long originalPrice,
                   String primaryImage, String hoverImage,
                   List<String> gallery, List<String> sizes, List<Color> colors,
                   String description, String material,
                   int stock, boolean isNew, boolean isFeatured) {
        this.slug = slug;
        this.name = name;
        this.category = category;
        this.collection = collection;
        this.price = price;
        this.originalPrice = originalPrice;
        this.primaryImage = primaryImage;
        this.hoverImage = hoverImage;
        this.gallery = gallery == null ? new ArrayList<>() : new ArrayList<>(gallery);
        this.sizes = sizes == null ? new ArrayList<>() : new ArrayList<>(sizes);
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        this.description = description;
        this.material = material;
        this.stock = stock;
        this.isNew = isNew;
        this.isFeatured = isFeatured;
    }

    // Record-style accessors (so existing callers `product.name()` keep working)
    public Long getId() { return id; }
    public Long id() { return id; }
    public String slug() { return slug; }
    public String name() { return name; }
    public Category category() { return category; }
    public Category getCategory() { return category; }
    /** Tiện ích — trả về slug từ relation. Để template & code cũ vẫn dùng được {@code product.categorySlug()}. */
    public String categorySlug() { return category != null ? category.slug() : null; }
    public String collection() { return collection; }
    public long price() { return price; }
    public Long originalPrice() { return originalPrice; }
    public String primaryImage() { return primaryImage; }
    public String hoverImage() { return hoverImage; }
    public List<String> gallery() { return gallery; }
    public List<String> sizes() { return sizes; }
    public List<Color> colors() { return colors; }
    public String description() { return description; }
    public String material() { return material; }
    public int stock() { return stock; }
    public boolean isNew() { return isNew; }
    public boolean isFeatured() { return isFeatured; }

    // Setters for admin form binding
    public void setSlug(String slug) { this.slug = slug; }
    public void setName(String name) { this.name = name; }
    public void setCategory(Category category) { this.category = category; }
    public void setCollection(String collection) { this.collection = collection; }
    public void setPrice(long price) { this.price = price; }
    public void setOriginalPrice(Long originalPrice) { this.originalPrice = originalPrice; }
    public void setPrimaryImage(String primaryImage) { this.primaryImage = primaryImage; }
    public void setHoverImage(String hoverImage) { this.hoverImage = hoverImage; }
    public void setGallery(List<String> gallery) { this.gallery = gallery; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
    public void setColors(List<Color> colors) { this.colors = colors; }
    public void setDescription(String description) { this.description = description; }
    public void setMaterial(String material) { this.material = material; }
    public void setStock(int stock) { this.stock = stock; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
    public void setFeatured(boolean isFeatured) { this.isFeatured = isFeatured; }

    // Computed fields
    public boolean onSale() {
        return originalPrice != null && originalPrice > price;
    }

    public int discountPercent() {
        if (!onSale()) return 0;
        return (int) Math.round(100.0 * (originalPrice - price) / originalPrice);
    }

    public String priceFormatted() {
        return formatVnd(price);
    }

    public String originalPriceFormatted() {
        return originalPrice == null ? "" : formatVnd(originalPrice);
    }

    public static String formatVnd(long amount) {
        return String.format("%,d", amount).replace(',', '.') + "₫";
    }
}
