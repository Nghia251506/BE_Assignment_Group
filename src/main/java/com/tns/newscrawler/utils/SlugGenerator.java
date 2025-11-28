package com.tns.newscrawler.utils;
import com.github.slugify.Slugify;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;  // Import đúng để dùng .test()

@Component
public class SlugGenerator {

    private static final Slugify SLUGIFY = Slugify.builder()
            .lowerCase(true)
            .build();

    public String make(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return SLUGIFY.slugify(text.trim());
    }

    public String makeUnique(String text, Predicate<String> existsChecker) {
        String slug = make(text);
        if (slug.isEmpty()) return "";

        String baseSlug = slug;
        int counter = 1;
        while (existsChecker.test(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }
}