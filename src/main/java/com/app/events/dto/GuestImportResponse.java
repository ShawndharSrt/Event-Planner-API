package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestImportResponse {
    private String message;
    private String eventTitle;
    private int insertedCount;
    private int duplicateCount;
    private List<String> duplicateEmails;
}
