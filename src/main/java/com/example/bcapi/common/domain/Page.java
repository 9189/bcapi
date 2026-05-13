package com.example.bcapi.common.domain;

import java.util.List;

public record Page<T>(List<T> items, int page, int size, boolean hasMore) {}
