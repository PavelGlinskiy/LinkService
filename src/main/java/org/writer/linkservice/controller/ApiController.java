package org.writer.linkservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.writer.linkservice.dto.UrlRequest;
import org.writer.linkservice.dto.UrlResponse;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.service.UrlService;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    private final UrlService urlService;

    public ApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shorten(@RequestBody UrlRequest request, UriComponentsBuilder uriBuilder) {
            UrlMapping mapping = urlService.createShortLink(
                    request.getUrl(),
                    request.getAlias(),
                    request.getTtlSeconds()
            );

            String shortPath = mapping.getShortCode();
            String shortUrl = uriBuilder.path("/{code}")
                    .buildAndExpand(shortPath)
                    .toUriString();

            return ResponseEntity.ok(
                    new UrlResponse(shortUrl, mapping.getAlias(), mapping.getExpiresAt())
            );
    }
}
