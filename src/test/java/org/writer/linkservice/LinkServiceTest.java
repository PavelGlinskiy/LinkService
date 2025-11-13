package org.writer.linkservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.repository.UrlMappingRepository;
import org.writer.linkservice.service.UrlService;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LinkServiceTest {
    private UrlMappingRepository repository;
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(UrlMappingRepository.class);
        urlService = new UrlService(repository, "http://localhost:8080");
    }

    @Test
    void createShortLink_WithAlias_SavesAndReturns() {
        when(repository.existsByAlias("myalias")).thenReturn(false);

        UrlMapping result = urlService.createShortLink("https://example.com", "myalias", 3600L);

        assertEquals("myalias", result.getAlias());
        assertEquals("myalias", result.getShortCode());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getExpiresAt().isAfter(Instant.now()));

        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortLink_WithoutAlias_GeneratesCode() {
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        UrlMapping result = urlService.createShortLink("https://example.com", null, null);

        assertNotNull(result.getShortCode());
        assertNull(result.getAlias());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getExpiresAt());

        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void findValidByShortCode_ReturnsOnlyIfNotExpired() {
        UrlMapping validMapping = new UrlMapping();
        validMapping.setShortCode("code1");
        validMapping.setExpiresAt(Instant.now().plusSeconds(100));

        UrlMapping expiredMapping = new UrlMapping();
        expiredMapping.setShortCode("code2");
        expiredMapping.setExpiresAt(Instant.now().minusSeconds(100));

        when(repository.findByShortCode("code1")).thenReturn(Optional.of(validMapping));
        when(repository.findByShortCode("code2")).thenReturn(Optional.of(expiredMapping));

        Optional<UrlMapping> res1 = urlService.findValidByShortCode("code1");
        Optional<UrlMapping> res2 = urlService.findValidByShortCode("code2");

        assertTrue(res1.isPresent());
        assertFalse(res2.isPresent());
    }
}
