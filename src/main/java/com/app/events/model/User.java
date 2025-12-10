package com.app.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user")
public class User extends BaseEntity {
    private String userId; // Auto-generated: AB00001, AB00002, etc.
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private List<String> role;
    private String avatar;
}
