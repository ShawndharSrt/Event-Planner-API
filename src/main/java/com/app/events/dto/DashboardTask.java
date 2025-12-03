package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTask {
    private String id;
    private String title;
    private boolean completed;
    private String priority;
}

