package org.writer.linkservice.controller;

import jakarta.validation.Valid;
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

public class ApiController {
    private final UrlService urlService;

    public ApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public UrlResponse shorten(@Valid @RequestBody UrlRequest request, UriComponentsBuilder uriBuilder) {

            UrlMapping mapping = urlService.createShortLink(
                    request.getUrl(),
                    request.getAlias(),
                    request.getTtlSeconds()
            );

        String shortUrl = uriBuilder
                .replacePath("/{code}")
                .buildAndExpand(mapping.getShortCode())
                .toUriString();

        return new UrlResponse(
                shortUrl,
                mapping.getAlias(),
                mapping.getExpiresAt()
        );
    }
}
