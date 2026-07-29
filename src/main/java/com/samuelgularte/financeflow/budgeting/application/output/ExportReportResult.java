package com.samuelgularte.financeflow.budgeting.application.output;

public record ExportReportResult(byte[] content, int year, int month) {
}
