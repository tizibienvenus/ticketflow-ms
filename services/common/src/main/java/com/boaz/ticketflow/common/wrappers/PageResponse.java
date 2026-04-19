package com.boaz.ticketflow.common.wrappers;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
            .content(List.of())
            .number(0)
            .size(0)
            .totalElements(0)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }

    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
            .content(content)
            .number(page.getNumber())              // numéro de page actuel
            .size(page.getSize())                  // taille de la page
            .totalElements(page.getTotalElements()) // total éléments
            .totalPages(page.getTotalPages())     // total pages
            .first(page.isFirst())                 // est la première page ?
            .last(page.isLast())                   // est la dernière page ?
            .build();
    }

}