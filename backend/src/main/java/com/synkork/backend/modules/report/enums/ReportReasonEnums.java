package com.synkork.backend.modules.report.enums;

import lombok.Getter;

@Getter
public enum ReportReasonEnums {
    SPAM(ReportSeverityEnums.LOW),
    INAPPROPRIATE(ReportSeverityEnums.MEDIUM),
    HARASSMENT(ReportSeverityEnums.HIGH),
    HATE_SPEECH(ReportSeverityEnums.CRITICAL),
    OTHER(ReportSeverityEnums.LOW);

    private final ReportSeverityEnums defaultSeverity;

    ReportReasonEnums(ReportSeverityEnums defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }
}