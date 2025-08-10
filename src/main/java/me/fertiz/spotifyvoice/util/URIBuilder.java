package me.fertiz.spotifyvoice.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class URIBuilder {

    private final String base;
    private final Map<String, String> params = new LinkedHashMap<>();

    private URIBuilder(String base) {
        this.base = base;
    }

    public URIBuilder addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public String build() {
        if (params.isEmpty()) return base;
        String query = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        return base + "?" + query;
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static URIBuilder create(String base) {
        return new URIBuilder(base);
    }
}

