package org.writer.linkservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.exception.ExpiredShortUrlException;
import org.writer.linkservice.exception.NotFoundShortUrlException;
import org.writer.linkservice.repository.UrlMappingRepository;
import org.writer.linkservice.service.UrlService;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class UrlServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("urlshortener")
            .withUsername("postgres")
            .withPassword("rootroot");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UrlService urlService;

    @Autowired
    private UrlMappingRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void createShortLink_WithAlias_SavesAndFinds() {
        UrlMapping mapping = urlService.createShortLink("https://example.com", "myalias", 3600L);

        assertEquals("myalias", mapping.getAlias());
        assertEquals("myalias", mapping.getShortCode());
        assertNotNull(mapping.getCreatedAt());
        assertNotNull(mapping.getExpiresAt());

        String originalUrl = urlService.getValidByShortCode("myalias");
        assertEquals("https://example.com", originalUrl);
    }

    @Test
    void createShortLink_WithoutAlias_GeneratesShortCode() {
        UrlMapping mapping = urlService.createShortLink("https://example.com", null, null);

        assertNotNull(mapping.getShortCode());
        assertNull(mapping.getAlias());
        assertNull(mapping.getExpiresAt());

        String originalUrl = urlService.getValidByShortCode(mapping.getShortCode());
        assertEquals("https://example.com", originalUrl);
    }

    @Test
    void getValidByShortCode_ThrowsExpiredShortUrlException_WhenExpired() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("expired")
                .originalUrl("https://example.com")
                .createdAt(OffsetDateTime.now().minusMinutes(10))
                .expiresAt(OffsetDateTime.now().minusSeconds(10))
                .build();

        repository.save(mapping);

        assertThrows(ExpiredShortUrlException.class, () ->
                urlService.getValidByShortCode("expired"));
    }

    @Test
    void getValidByShortCode_ThrowsNotFoundShortUrlException_WhenMissing() {
        assertThrows(NotFoundShortUrlException.class, () ->
                urlService.getValidByShortCode("missing"));
    }
}
