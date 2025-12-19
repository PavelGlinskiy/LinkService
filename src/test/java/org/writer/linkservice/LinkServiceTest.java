package org.writer.linkservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.exception.AliasAlreadyInUseException;
import org.writer.linkservice.exception.EmptyOriginalUrlException;
import org.writer.linkservice.exception.ExpiredShortUrlException;
import org.writer.linkservice.exception.NotFoundShortUrlException;
import org.writer.linkservice.repository.UrlMappingRepository;
import org.writer.linkservice.service.UrlService;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        UrlMapping mappingToSave = UrlMapping.builder()
                .alias("myalias")
                .shortCode("myalias")
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusSeconds(3600))
                .build();

        when(repository.save(any(UrlMapping.class))).thenReturn(mappingToSave);

        UrlMapping result = urlService.createShortLink("https://example.com", "myalias", 3600L);

        assertEquals("myalias", result.getAlias());
        assertEquals("myalias", result.getShortCode());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExpiresAt());
        assertTrue(result.getExpiresAt().isAfter(OffsetDateTime.now()));

        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortLink_WithoutAlias_GeneratesCode() {
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        UrlMapping mappingToSave = UrlMapping.builder()
                .shortCode("random123")
                .createdAt(OffsetDateTime.now())
                .build();

        when(repository.save(any(UrlMapping.class))).thenReturn(mappingToSave);

        UrlMapping result = urlService.createShortLink("https://example.com", null, null);

        assertNotNull(result.getShortCode());
        assertNull(result.getAlias());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getExpiresAt());

        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void createShortLink_EmptyOriginalUrl_ThrowsException() {
        assertThatThrownBy(() -> urlService.createShortLink("", null, null))
                .isInstanceOf(EmptyOriginalUrlException.class);
    }

    @Test
    void createShortLink_AliasAlreadyExists_ThrowsException() {
        when(repository.save(any(UrlMapping.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> urlService.createShortLink("https://example.com", "alias", null))
                .isInstanceOf(AliasAlreadyInUseException.class);
    }

    @Test
    void getValidByShortCode_ReturnsOriginalUrl_IfExistsAndNotExpired() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("code1")
                .originalUrl("https://example.com")
                .expiresAt(OffsetDateTime.now().plusSeconds(100))
                .build();

        when(repository.findByShortCode("code1")).thenReturn(Optional.of(mapping));

        String originalUrl = urlService.getValidByShortCode("code1");

        assertEquals("https://example.com", originalUrl);
    }

    @Test
    void getValidByShortCode_ThrowsNotFoundIfMissing() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getValidByShortCode("missing"))
                .isInstanceOf(NotFoundShortUrlException.class);
    }

    @Test
    void getValidByShortCode_ThrowsExpiredIfExpired() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("code2")
                .originalUrl("https://example.com")
                .expiresAt(OffsetDateTime.now().minusSeconds(100))
                .build();

        when(repository.findByShortCode("code2")).thenReturn(Optional.of(mapping));

        assertThatThrownBy(() -> urlService.getValidByShortCode("code2"))
                .isInstanceOf(ExpiredShortUrlException.class);
    }
}
