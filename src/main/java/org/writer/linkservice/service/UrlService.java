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
            return createWithAlias(originalUrl, alias, ttlSeconds);
        }

        return createWithRandomCode(originalUrl, ttlSeconds);
    }

    private UrlMapping createWithAlias(String originalUrl, String alias, Long ttlSeconds) {
        UrlMapping mapping = buildMapping(originalUrl, alias, alias, ttlSeconds);
        try {
            return repository.save(mapping);
        } catch (DataIntegrityViolationException ex) {
            throw new AliasAlreadyInUseException(alias);
        }
    }

    private UrlMapping createWithRandomCode(String originalUrl, Long ttlSeconds) {
        String code = generateUniqueShortCode();
        UrlMapping mapping = buildMapping(originalUrl, code, null, ttlSeconds);
        return repository.save(mapping);
    }

    private String generateUniqueShortCode() {
        int codeLength = defaultCodeLength;
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = ShortCodeGenerator.randomCode(codeLength);
            if (!repository.existsByShortCode(code)) {
                return code;
            }
        }
        return ShortCodeGenerator.randomCode(defaultCodeLength + 3);
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
