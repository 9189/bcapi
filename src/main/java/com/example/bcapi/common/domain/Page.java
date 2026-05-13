package com.example.bcapi.common.domain;

import java.util.List;

public record Page<T>(List<T> items, int offset, int limit, boolean hasMore) {}
