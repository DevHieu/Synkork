package com.synkork.backend.modules.report;

import com.synkork.backend.modules.report.dtos.ReportRequestDto;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/users")
    public ResponseEntity<Void> createUserReport(@RequestBody ReportRequestDto request) {
        reportService.createReport(request, ReportTypeEnums.USER);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }   

    @PostMapping("/rooms")
    public ResponseEntity<Void> createRoomReport(@RequestBody ReportRequestDto request) {
        reportService.createReport(request, ReportTypeEnums.ROOM);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
