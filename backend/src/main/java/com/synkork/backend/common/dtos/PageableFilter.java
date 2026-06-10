package com.synkork.backend.common.dtos;

public interface PageableFilter {
    Integer page();
    Integer size();

    default int getPage() {
        return page() != null ? page() : 0;
    }

    default int getSize() {
        return size() != null ? size() : 20;
    }
}