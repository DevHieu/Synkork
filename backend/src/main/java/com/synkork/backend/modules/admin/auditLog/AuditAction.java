package com.synkork.backend.modules.admin.auditLog;

public final class AuditAction {

    // User management
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String BAN_USER = "BAN_USER";
    public static final String UNBAN_USER = "UNBAN_USER";

    // Workspace/Room management
    public static final String UPDATE_WORKSPACE = "UPDATE_WORKSPACE";
    public static final String DELETE_WORKSPACE = "DELETE_WORKSPACE";

    // Subscription management
    public static final String UPDATE_SUBSCRIPTION = "UPDATE_SUBSCRIPTION";
    public static final String CANCEL_SUBSCRIPTION = "CANCEL_SUBSCRIPTION";

    // Report handling
    public static final String RESOLVE_REPORT = "RESOLVE_REPORT";
    public static final String DISMISS_REPORT = "DISMISS_REPORT";

    private AuditAction() {}
}