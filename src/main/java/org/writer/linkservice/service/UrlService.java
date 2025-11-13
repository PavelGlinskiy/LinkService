package org.writer.linkservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.repository.UrlMappingRepository;
import org.writer.linkservice.util.ShortCodeGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
        if (originalUrl == null || originalUrl.isBlank())
            throw new IllegalArgumentException("originalUrl required");

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);

        if (ttlSeconds != null && ttlSeconds > 0) {
            mapping.setExpiresAt(Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS));
        }

        if (alias != null && !alias.isBlank()) {
            if (repository.existsByAlias(alias)) {
                throw new IllegalArgumentException("alias already in use");
            }
            mapping.setAlias(alias);
            mapping.setShortCode(alias);
            return repository.save(mapping);
        }

        String code;
        int tries = 0;
        do {
            code = ShortCodeGenerator.randomCode(defaultCodeLength);
            tries++;
            if (tries > 10) {
                code = ShortCodeGenerator.randomCode(defaultCodeLength + 3);
            }
        } while (repository.existsByShortCode(code));

        mapping.setShortCode(code);
        return repository.save(mapping);
    }

    public Optional<UrlMapping> findValidByShortCode(String shortCode) {
        return repository.findByShortCode(shortCode)
                .filter(mapping -> !mapping.isExpired());
    }
}
