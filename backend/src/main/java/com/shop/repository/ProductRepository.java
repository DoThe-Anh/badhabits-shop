package com.shop.repository;

import com.shop.entity.Category;
import com.shop.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /**
     * Lock dòng product khi đặt hàng → tránh oversell khi 2 user mua cùng lúc.
     * Phải gọi trong transaction (placeOrder đã có @Transactional).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("select p from Product p where p.category.slug = :slug")
    List<Product> findByCategorySlug(@Param("slug") String slug);

    @Query(value = "select p from Product p where p.category.slug = :slug",
           countQuery = "select count(p) from Product p where p.category.slug = :slug")
    Page<Product> findByCategorySlug(@Param("slug") String slug, Pageable pageable);

    /**
     * Combined search: (q match name/slug) AND (categorySlug filter), cả hai đều optional.
     * - q null/blank → bỏ qua điều kiện q.
     * - categorySlug null/blank → bỏ qua filter category.
     * - Cả hai null → trả về tất cả (như findAll).
     */
    @Query(value = """
            select p from Product p
            where (:q is null or :q = ''
                   or lower(p.name) like lower(concat('%', :q, '%'))
                   or lower(p.slug) like lower(concat('%', :q, '%')))
              and (:categorySlug is null or :categorySlug = ''
                   or p.category.slug = :categorySlug)
            """,
           countQuery = """
            select count(p) from Product p
            where (:q is null or :q = ''
                   or lower(p.name) like lower(concat('%', :q, '%'))
                   or lower(p.slug) like lower(concat('%', :q, '%')))
              and (:categorySlug is null or :categorySlug = ''
                   or p.category.slug = :categorySlug)
            """)
    Page<Product> search(@Param("q") String q,
                         @Param("categorySlug") String categorySlug,
                         Pageable pageable);

    long countByCategory(Category category);

    long countByStockLessThan(int threshold);

    @Query("select coalesce(sum(p.stock), 0) from Product p")
    long sumAllStock();

    List<Product> findByCollection(String collection);

    @Query("select p from Product p where p.isNew = true")
    List<Product> findNewArrivals(Pageable pageable);

    @Query("select p from Product p where p.isFeatured = true")
    List<Product> findFeatured(Pageable pageable);

    @Query("select p from Product p where p.originalPrice is not null and p.price < p.originalPrice")
    List<Product> findOnSale(Pageable pageable);

    @Query("select p from Product p where p.category.slug = :categorySlug and p.id <> :excludeId order by p.isFeatured desc")
    List<Product> findRelated(@Param("categorySlug") String categorySlug,
                              @Param("excludeId") Long excludeId,
                              Pageable pageable);
}
