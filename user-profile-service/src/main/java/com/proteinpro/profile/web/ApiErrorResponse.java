package com.proteinpro.profile.web;

public record ApiErrorResponse(String timestamp, int status, String error, String message, String path) {
}
