package org.writer.linkservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.exception.AliasAlreadyInUseException;
import org.writer.linkservice.exception.EmptyOriginalUrlException;
import org.writer.linkservice.exception.ExpiredShortUrlException;
import org.writer.linkservice.exception.NotFoundShortUrlException;
import org.writer.linkservice.repository.UrlMappingRepository;
import org.writer.linkservice.util.ShortCodeGenerator;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UrlService {
    private final UrlMappingRepository repository;
    private final int defaultCodeLength = 7;
    private final String baseUrl;

    public UrlService(UrlMappingRepository repository,
                      @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.repository = repository;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public UrlMapping createShortLink(String originalUrl, String alias, Long ttlSeconds) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new EmptyOriginalUrlException();
        }

        if (alias != null && !alias.isBlank()) {
            UrlMapping mapping = buildMapping(originalUrl, alias, alias, ttlSeconds);
            try {
                return repository.save(mapping);
            } catch (DataIntegrityViolationException ex) {
                throw new AliasAlreadyInUseException(alias);
            }
        }
        return createWithGeneratedShortCode(originalUrl, ttlSeconds);
    }

    private UrlMapping createWithGeneratedShortCode(String originalUrl, Long ttlSeconds) {

        int attempts = 0;
        int maxAttempts = 20;
        int length = defaultCodeLength;

        while (attempts < maxAttempts) {

            if (attempts == 10) {
                length++;
            }

            String code = ShortCodeGenerator.randomCode(length);
            UrlMapping mapping = buildMapping(originalUrl, code, null, ttlSeconds);

            try {
                return repository.saveAndFlush(mapping);
            }
            catch (DataIntegrityViolationException ex) {
                attempts++;
            }
        }

        String fallback = ShortCodeGenerator.fallbackCode();
        UrlMapping fallbackMapping = buildMapping(originalUrl, fallback, null, ttlSeconds);

        try {
            return repository.saveAndFlush(fallbackMapping);
        } catch (Exception ex) {
            throw new RuntimeException("Could not generate unique short URL even with fallback", ex);
        }
    }


    private UrlMapping buildMapping(String originalUrl, String shortCode, String alias, Long ttlSeconds) {
        OffsetDateTime expiresAt = ttlSeconds != null && ttlSeconds > 0
                ? OffsetDateTime.now().plusSeconds(ttlSeconds)
                : null;

        return UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .alias(alias)
                .expiresAt(expiresAt)
                .build();
    }

    public String getValidByShortCode(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new NotFoundShortUrlException(shortCode));

        if (mapping.isExpired()) {
            throw new ExpiredShortUrlException(shortCode);
        }

        return mapping.getOriginalUrl();
    }
}
