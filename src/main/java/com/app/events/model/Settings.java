package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "setting")
public class Settings extends BaseEntity {
    private String eventId;
    private String userId;
    private String preferencesJson;
}

