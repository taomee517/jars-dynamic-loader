package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 罗涛
 * @title IndexController
 * @date 2020/12/8 10:02
 */

@RestController
@RequestMapping("demo")
public class IndexController {

    @GetMapping("index")
    public String index(){
        return "Hello,Jars Importing!";
    }
}
