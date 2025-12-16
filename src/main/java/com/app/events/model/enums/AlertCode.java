package com.app.events.model.enums;

import lombok.Getter;

@Getter
public enum AlertCode {
    SMUA("SMUA", "System Maintenance Update Alert", Severity.INFO, AlertType.ONLINE, null),
    TDDA("TDDA", "Task Due Date Alert", Severity.WARNING, AlertType.OFFLINE, null),
    B8EA("B8EA", "Budget 80% Exceed Alert", Severity.WARNING, AlertType.OFFLINE, null),
    B1EA("B1EA", "Budget 100% Exceed Alert", Severity.CRITICAL, AlertType.ONLINE, null),
    EPDA("EPDA", "Expenses Payment Due Alert", Severity.WARNING, AlertType.OFFLINE, null),
    TCA("TCA", "Task Completed Alert", Severity.INFO, AlertType.ONLINE, null),
    TOA("TOA", "Task Overdue Alert", Severity.CRITICAL, AlertType.OFFLINE, null),
    BOVA("BOVA", "Budget Overrun Alert", Severity.CRITICAL, AlertType.ONLINE, null),
    EITA("EITA", "Invitation Triggered Alert", Severity.INFO, AlertType.ONLINE, null);

    private final String code;
    private final String description;
    private final Severity severity;
    private final AlertType type;
    private final String subType;

    AlertCode(String code, String description, Severity severity, AlertType type, String subType) {
        this.code = code;
        this.description = description;
        this.severity = severity;
        this.type = type;
        this.subType = subType;
    }

    public enum Severity {
        INFO, WARNING, CRITICAL
    }

    public enum AlertType {
        ONLINE, OFFLINE
    }
}
