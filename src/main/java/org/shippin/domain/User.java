package org.shippin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shippin.domain.enums.Role;

@Data
@AllArgsConstructor
public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password = "default";
    private Role role;
    private String accessToken;

    public User( String firstName,String lastName, String email, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
}

