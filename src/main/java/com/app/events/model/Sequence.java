package com.app.events.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "sequence")
public class Sequence {
    @Id
    private String id;
    private long seq;
}
