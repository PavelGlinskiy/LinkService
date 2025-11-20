package org.writer.linkservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.writer.linkservice.exception.InvalidShortCodeException;
import org.writer.linkservice.service.UrlService;

@Controller
public class RedirectController {
    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{code}")
    public String redirect(@PathVariable String code){

        if (!code.matches("^[a-zA-Z0-9_-]+$")) {
            throw new InvalidShortCodeException(code);
        }

        String originalUrl = urlService.getValidByShortCode(code);

        return "redirect:" + originalUrl;
    }
}
