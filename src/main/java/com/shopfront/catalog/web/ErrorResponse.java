package com.shopfront.catalog.web;

import java.time.Instant;
import java.util.List;

/** Error body of every 4xx. The web app shows {@code message}; {@code fieldErrors} only on 400. */
public record ErrorResponse(String error, String message, List<String> fieldErrors, Instant timestamp) {

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, List.of(), Instant.now());
    }
}
