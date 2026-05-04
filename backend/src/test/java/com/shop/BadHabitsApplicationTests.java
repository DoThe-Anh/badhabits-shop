package com.shop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verify Spring context load thành công với H2 + Hibernate ddl-auto.
 * Không assert gì — Spring sẽ fail test nếu wiring có vấn đề (missing bean,
 * circular dep, broken entity mapping, ...).
 */
@SpringBootTest
@ActiveProfiles("test")
class BadHabitsApplicationTests {

    @Test
    void contextLoads() {
        // Trống có chủ đích — chỉ cần context start được.
    }
}
