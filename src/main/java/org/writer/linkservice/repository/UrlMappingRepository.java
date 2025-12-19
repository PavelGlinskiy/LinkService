package org.writer.linkservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.writer.linkservice.entity.UrlMapping;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    Optional<UrlMapping> findByAlias(String alias);
    boolean existsByShortCode(String shortCode);
    boolean existsByAlias(String alias);
}
