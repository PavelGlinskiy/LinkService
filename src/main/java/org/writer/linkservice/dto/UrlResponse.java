package org.writer.linkservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class UrlResponse {
    private String shortUrl;
    private String alias;
    private Instant expiresAt;
}
