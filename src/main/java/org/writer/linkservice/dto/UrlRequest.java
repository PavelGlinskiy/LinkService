package org.writer.linkservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UrlRequest {
    private String url;
    private String alias;
    private Long ttlSeconds;
}
