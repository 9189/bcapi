package com.example.bcapi.beer.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

class BeerSearchSpecification implements Specification<BeerEntity> {

    private final String pattern;

    BeerSearchSpecification(String term) {
        this.pattern = term == null || term.isBlank() ? null : "%" + term.toLowerCase() + "%";
    }

    @Override
    public Predicate toPredicate(
            @NonNull Root<BeerEntity> root,
            @NonNull CriteriaQuery<?> query,
            @NonNull CriteriaBuilder cb
    ) {
        if (pattern == null) {
            return null;
        }
        return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("type")), pattern),
                cb.like(cb.function("str", String.class, root.get("abv")), pattern),
                cb.like(cb.lower(root.join("manufacturer").get("name")), pattern)
        );
    }
}
