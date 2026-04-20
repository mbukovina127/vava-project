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
    private Role role;

    public User(Integer id, String firstName, String email, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.email = email;
        this.role = role;
    }

}

