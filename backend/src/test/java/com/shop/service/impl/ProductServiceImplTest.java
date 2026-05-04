package com.shop.service.impl;

import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl service;

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private static final Category CAT_TOPS = makeCategory(1L, "tops", "TOPS");

    private static Category makeCategory(Long id, String slug, String name) {
        Category c = new Category(slug, name, "");
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    /** Tạo Product với chỉ những field cần cho test. */
    private static Product product(Long id, String name, long price, boolean isNew) {
        Product p = new Product(
                "slug-" + id, name, CAT_TOPS, "HO25",
                price, null, "/img/p.webp", null,
                List.of(), List.of(), List.of(),
                "desc", "material",
                10, isNew, false
        );
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    // -----------------------------------------------------------------
    // sorted() — chỉ thuần logic Java, không dùng repo
    // -----------------------------------------------------------------

    @Test
    void sorted_priceAsc_returnsAscendingByPrice() {
        List<Product> input = List.of(
                product(1L, "C", 300_000L, false),
                product(2L, "A", 100_000L, false),
                product(3L, "B", 200_000L, false)
        );

        List<Product> result = service.sorted(input, "price-asc");

        assertThat(result).extracting(Product::price)
                .containsExactly(100_000L, 200_000L, 300_000L);
    }

    @Test
    void sorted_priceDesc_returnsDescendingByPrice() {
        List<Product> input = List.of(
                product(1L, "A", 100_000L, false),
                product(2L, "B", 300_000L, false),
                product(3L, "C", 200_000L, false)
        );

        List<Product> result = service.sorted(input, "price-desc");

        assertThat(result).extracting(Product::price)
                .containsExactly(300_000L, 200_000L, 100_000L);
    }

    @Test
    void sorted_newest_putsIsNewTrueFirst() {
        Product oldA = product(1L, "Old A", 100_000L, false);
        Product newB = product(2L, "New B", 100_000L, true);
        Product oldC = product(3L, "Old C", 100_000L, false);

        List<Product> result = service.sorted(List.of(oldA, newB, oldC), "newest");

        // isNew=true sort trước theo Comparator.comparing(Product::isNew).reversed()
        assertThat(result.get(0).isNew()).isTrue();
        assertThat(result.subList(1, 3)).extracting(Product::isNew)
                .containsOnly(false);
    }

    @Test
    void sorted_unknownSortKey_returnsInputOrderUnchanged() {
        List<Product> input = List.of(
                product(1L, "C", 300_000L, false),
                product(2L, "A", 100_000L, false)
        );

        List<Product> result = service.sorted(input, "anything-else");

        assertThat(result).containsExactlyElementsOf(input);
    }

    @Test
    void sorted_nullSort_returnsInputUnchanged() {
        List<Product> input = List.of(product(1L, "X", 100_000L, false));

        assertThat(service.sorted(input, null)).isSameAs(input);
    }

    // -----------------------------------------------------------------
    // Keyword filters: findSummer / findWinter / findHoodies
    // -----------------------------------------------------------------

    @Test
    void findSummer_keepsOnlyProductsStartingWithSet() {
        when(productRepository.findAll()).thenReturn(List.of(
                product(1L, "Set áo + quần short A", 0L, false),
                product(2L, "Áo hoodie B",            0L, false),
                product(3L, "Set ngũ vị hương",       0L, false),
                product(4L, "set viết thường",        0L, false)  // case-sensitive: "Set " vs "set "
        ));

        List<Product> result = service.findSummer(10);

        // findSummer dùng `startsWith("Set ")` — case sensitive, "set" thường KHÔNG match
        assertThat(result).extracting(Product::name)
                .containsExactlyInAnyOrder("Set áo + quần short A", "Set ngũ vị hương");
    }

    @Test
    void findWinter_matchesAnyVietnameseKeyword() {
        when(productRepository.findAll()).thenReturn(List.of(
                product(1L, "Áo khoác bomber",   0L, false),
                product(2L, "Áo thun basic",     0L, false),  // không match
                product(3L, "Quần jeans dài",    0L, false),
                product(4L, "Áo len cao cổ",     0L, false),
                product(5L, "Quần kaki dài",     0L, false),
                product(6L, "Quần short",        0L, false),  // không match
                product(7L, "Áo Puffer Arctic",  0L, false)
        ));

        List<Product> result = service.findWinter(10);

        assertThat(result).extracting(Product::name).containsExactlyInAnyOrder(
                "Áo khoác bomber",
                "Quần jeans dài",
                "Áo len cao cổ",
                "Quần kaki dài",
                "Áo Puffer Arctic"
        );
    }

    @Test
    void findHoodies_matchesCaseInsensitiveSubstring() {
        when(productRepository.findAll()).thenReturn(List.of(
                product(1L, "Hoodie Essential",       0L, false),
                product(2L, "HOODIE Riot oversize",   0L, false),
                product(3L, "Áo hoodie pullover",     0L, false),
                product(4L, "Áo thun basic",          0L, false)  // không match
        ));

        List<Product> result = service.findHoodies(10);

        assertThat(result).extracting(Product::name).containsExactlyInAnyOrder(
                "Hoodie Essential",
                "HOODIE Riot oversize",
                "Áo hoodie pullover"
        );
    }

    @Test
    void findHoodies_respectsLimit() {
        when(productRepository.findAll()).thenReturn(List.of(
                product(1L, "hoodie 1", 0L, false),
                product(2L, "hoodie 2", 0L, false),
                product(3L, "hoodie 3", 0L, false),
                product(4L, "hoodie 4", 0L, false)
        ));

        List<Product> result = service.findHoodies(2);

        assertThat(result).hasSize(2);
    }

    // -----------------------------------------------------------------
    // findRelated — verify args truyền đúng vào repo
    // -----------------------------------------------------------------

    @Test
    void findRelated_passesCategorySlugAndExcludesProductId() {
        Product seed = product(42L, "Some product", 0L, false);
        when(productRepository.findRelated(eq("tops"), eq(42L), any(Pageable.class)))
                .thenReturn(List.of(product(99L, "Other", 0L, false)));

        List<Product> result = service.findRelated(seed, 4);

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findRelated(eq("tops"), eq(42L), pageCaptor.capture());

        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(4);
        assertThat(pageCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(result).hasSize(1).first().extracting(Product::getId).isEqualTo(99L);
    }

    @Test
    void findRelated_clampsLimitTo1WhenNonPositive() {
        Product seed = product(1L, "x", 0L, false);
        when(productRepository.findRelated(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        service.findRelated(seed, 0);

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findRelated(any(), any(), pageCaptor.capture());

        assertThat(pageCaptor.getValue().getPageSize()).isEqualTo(1);
    }

    // -----------------------------------------------------------------
    // Delegations đơn giản — verify gọi repo đúng method
    // -----------------------------------------------------------------

    @Test
    void findBySlug_delegatesToRepository() {
        Product expected = product(7L, "X", 0L, false);
        when(productRepository.findBySlug("x")).thenReturn(Optional.of(expected));

        Optional<Product> result = service.findBySlug("x");

        assertThat(result).contains(expected);
        verify(productRepository).findBySlug("x");
    }

    @Test
    void findByCategory_delegatesToFindByCategorySlug() {
        when(productRepository.findByCategorySlug("tops"))
                .thenReturn(List.of(product(1L, "X", 0L, false)));

        List<Product> result = service.findByCategory("tops");

        assertThat(result).hasSize(1);
        verify(productRepository).findByCategorySlug("tops");
    }
}
