package com.github.unclecatmyself.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by MySelf on 2018/11/28.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String index(){
        return "Welcome to the home page!";
    }

}
