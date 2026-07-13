package com.synkork.backend.modules.report;

import com.synkork.backend.modules.report.dtos.ReportRequestDto;
import com.synkork.backend.modules.report.enums.ReportTypeEnums;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createUserReport(@ModelAttribute ReportRequestDto request, @RequestParam(required = false) MultipartFile evidence) {
        reportService.createReport(request, ReportTypeEnums.USER, evidence);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(value = "/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createRoomReport(@ModelAttribute ReportRequestDto request, @RequestParam(value = "evidence", required = false) MultipartFile evidence) {
        reportService.createReport(request, ReportTypeEnums.ROOM, evidence);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
