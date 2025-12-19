package org.writer.linkservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UrlRequest {

    @NotBlank(message = "URL must not be blank")
    private String url;

    @Size(max = 64, message = "Alias must be at most 64 characters")
    private String alias;
    private Long ttlSeconds;
}
