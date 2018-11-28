package com.github.unclecatmyself.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by MySelf on 2018/11/28.
 */
@Data
@Builder
@AllArgsConstructor
public class Greeting {

    private final long id;
    private final String content;

}
