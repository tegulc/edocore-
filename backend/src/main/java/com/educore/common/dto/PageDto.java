package com.educore.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Enveloppe de pagination standard, conforme au Volume 6 (Documentation API),
 * Partie II.2 : { "donnees": [...], "pagination": { "page", "taille", "total",
 * "pages_totales" } }, avec une page en base 1 (le premier element de la
 * collection est la page 1, jamais 0).
 *
 * Spring Data Page/Pageable est nativement en base 0 : la conversion se fait
 * exclusivement ici, pour que le reste du code (controllers, services) ne
 * manipule que des index Spring Data classiques et ne se trompe jamais de
 * convention.
 */
public record PageDto<T>(List<T> donnees, Pagination pagination) {

    public record Pagination(int page, int taille, long total, int pagesTotales) {}

    public static <T> PageDto<T> depuis(Page<T> page) {
        return new PageDto<>(page.getContent(),
                new Pagination(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    public static <T, R> PageDto<R> depuis(Page<T> page, Function<T, R> mapper) {
        return new PageDto<>(page.getContent().stream().map(mapper).toList(),
                new Pagination(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }
}
