package com.app.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentEvent {
    private String id;
    private String title;
    private String type;
    private String status;
    private LocalDate startDate;
    private String startTime;
    private String location;
    private String coverImage;
}

