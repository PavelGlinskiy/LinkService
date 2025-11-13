package org.writer.linkservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.writer.linkservice.controller.ApiController;
import org.writer.linkservice.controller.RedirectController;
import org.writer.linkservice.entity.UrlMapping;
import org.writer.linkservice.service.UrlService;
import org.springframework.http.MediaType;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = { ApiController.class, RedirectController.class })
public class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService urlService;

    @Test
    @DisplayName("POST /api/shorten — returns short code")
    void createShortLink_ReturnsShortCode() throws Exception {
        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode("xyz123");
        mapping.setOriginalUrl("https://google.com");

        Mockito.when(urlService.createShortLink("https://google.com", null, null))
                .thenReturn(mapping);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://google.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost/xyz123"));
    }

    @Test
    void redirectToOriginal_Redirects() throws Exception {
        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode("xyz123");
        mapping.setOriginalUrl("https://google.com");

        Mockito.when(urlService.findValidByShortCode("xyz123"))
                .thenReturn(Optional.of(mapping));

        mockMvc.perform(get("/xyz123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://google.com"));
    }

    @Test
    @DisplayName("GET /{code} — returns 404 if not found")
    void redirect_NotFound() throws Exception {

        Mockito.when(urlService.findValidByShortCode("bad123"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/bad123"))
                .andExpect(status().isNotFound());
    }
}