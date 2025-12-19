package org.writer.linkservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class UrlResponse {
    private String shortUrl;
    private String alias;
    private OffsetDateTime expiresAt;
}
