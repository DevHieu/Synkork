package com.synkork.backend.modules.admin.auditLog.enums;

public enum LogActionEnum {
    CREATE_INVOICE,
    UPDATE_INVOICE,
    DELETE_INVOICE,
    CANCEL_SUBSCRIPTION,
    UPDATE_SUBSCRIPTION,

    BAN_USER,
    UNBAN_USER,
    UPDATE_USER,
    DELETE_USER,

    UPDATE_WORKSPACE,
    DELETE_WORKSPACE,

    RESOLVE_REPORT,
    DISMISS_REPORT,
    REPORT_CREATED,
    REPORT_REVIEWED,
    REPORT_DELETED
}