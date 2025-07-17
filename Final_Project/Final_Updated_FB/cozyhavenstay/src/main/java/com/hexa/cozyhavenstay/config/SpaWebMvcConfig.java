// src/main/java/com/hexa/cozyhavenstay/config/SpaWebMvcConfig.java
package com.hexa.cozyhavenstay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Explicitly handle the root URL
        registry.addViewController("/").setViewName("forward:/");

        // Forward paths that look like client-side routes (i.e., no file extension)
        // to the root URL ("/") which will then serve index.html.

        // Handles /guest/anything (e.g., /guest/my-bookings)
        registry.addViewController("/guest/{path:[^\\.]*}").setViewName("forward:/");

        // Handles /admin/anything (e.g., /admin/dashboard)
        registry.addViewController("/admin/{path:[^\\.]*}").setViewName("forward:/");

        // Handles /owner/anything (e.g., /owner/dashboard)
        registry.addViewController("/owner/{path:[^\\.]*}").setViewName("forward:/");

        // This is the most general SPA fallback. It catches any single-segment path
        // that does not have a file extension (like .js, .css, .jpg)
        // and forwards it to the root ("/") which will then serve index.html.
        // It covers routes like /login, /some-other-angular-route directly.
        registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/");

        // Removed the problematic line: registry.addViewController("/**/{path:[^\\.]*}").setViewName("forward:/");
        // The above patterns, especially the general "/{path:[^\\.]*}", should cover most
        // Angular routes effectively without this conflicting pattern.
    }
}