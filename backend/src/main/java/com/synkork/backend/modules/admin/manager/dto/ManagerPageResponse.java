package com.synkork.backend.modules.admin.manager.dto;

import com.synkork.backend.modules.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ManagerPageResponse {

    private List<ManagerResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static ManagerPageResponse from(Page<UserEntity> result) {
        return ManagerPageResponse.builder()
                .content(result.getContent().stream().map(ManagerResponse::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }
}
