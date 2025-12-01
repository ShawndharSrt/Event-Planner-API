package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user")
public class User extends BaseEntity {
    private String name;
    private String email;
    private String password;
    private String role;
    private String avatar;
}
