package com.synkork.backend.modules.admin.room.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RoomFilterRequest {
    private String search;
    private String status;       // OPEN / CLOSED
    private LocalDate createdFrom; // lọc từ ngày
    private LocalDate createdTo;   // lọc đến ngày
    private Integer minMembers;    // số member tối thiểu
    private int page = 0;
    private int size = 20;
}