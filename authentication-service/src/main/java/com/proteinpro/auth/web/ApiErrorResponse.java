package com.proteinpro.auth.web;

public record ApiErrorResponse(String timestamp, int status, String error, String message, String path) {
}
