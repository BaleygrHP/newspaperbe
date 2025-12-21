package com.hungpham.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

    private SlugUtil() {}

    public static String slugIfy(String input) {
        if (input == null) return null;

        String noWhiteSpace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");

        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = EDGES_DASHES.matcher(slug).replaceAll("");

        return slug;
    }
}
