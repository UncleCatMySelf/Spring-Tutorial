package com.github.unclecatmyself.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by MySelf on 2018/11/21.
 */
@Data
@AllArgsConstructor
public class Greeting {

    private final long id;
    private final String content;

}
