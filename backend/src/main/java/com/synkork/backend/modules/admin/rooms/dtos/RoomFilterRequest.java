package com.synkork.backend.modules.admin.rooms.dtos;

import com.synkork.backend.common.dtos.PageableFilter;
import com.synkork.backend.modules.room.enums.RoomStatusEnum;
import com.synkork.backend.modules.room.enums.RoomTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record RoomFilterRequest(
        String search,
        RoomStatusEnum status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        @Min(value = 0, message = "Page phải >= 0")
        Integer page,

        @Min(value = 1, message = "Size phải >= 1")
        @Max(value = 100, message = "Size tối đa 100")
        Integer size,

        @Min(value = 0, message = "Size phải >= 0")
        Integer minMembers,

        @Min(value = 0, message = "Size phải >= 0")
        Integer maxMembers,

        @Min(value = 0, message = "Size phải >= 0")
        Integer minWarning,

        @Min(value = 0, message = "Size phải >= 0")
        Integer maxWarning
) implements PageableFilter {

    public void validate() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom phải nhỏ hơn hoặc bằng dateTo");
        }

        if (minMembers != null && maxMembers != null && minMembers > maxMembers) {
            throw new IllegalArgumentException("minMembers phải nhỏ hơn hoặc bằng maxMembers");
        }

        if (minWarning != null && maxWarning != null && minWarning > maxWarning) {
            throw new IllegalArgumentException("minWarning phải nhỏ hơn hoặc bằng maxWarning");
        }
    }
}