package com.github.unclecatmyself.controller;

import com.github.unclecatmyself.pojo.Greeting;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by MySelf on 2018/11/28.
 */
@Controller
public class HelloController {

    private static final String template = "Hello,%s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/hello-world")
    @ResponseBody
    public Greeting sayHello(@RequestParam(name = "name",required = false,defaultValue = "Stranger") String name){
        return new Greeting(counter.incrementAndGet(),String.format(template,name));
    }

}
