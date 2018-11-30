package com.github.unclecatmyself.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by MySelf on 2018/11/30.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonForm {

    @NotNull
    @Size(min = 2,max = 30)
    private String name;

    @NotNull
    @Min(18)
    private Integer age;

}
