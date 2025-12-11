package com.app.events.dto;

import lombok.Data;
import java.util.List;

@Data
public class AddGuestsRequest {
    private List<String> guestIds;
}
