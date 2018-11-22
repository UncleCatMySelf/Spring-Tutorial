package com.github.unclecatmyself.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by MySelf on 2018/11/22.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private long id;
    private String firstName,lastName;

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

}
