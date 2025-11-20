package org.writer.linkservice.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "url_mapping", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code", unique = true),
        @Index(name = "idx_alias", columnList = "alias", unique = true)
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UrlMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 64)
    private String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "text")
    private String originalUrl;

    @Column(name = "alias", length = 64, unique = true)
    private String alias;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now());
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }
}
