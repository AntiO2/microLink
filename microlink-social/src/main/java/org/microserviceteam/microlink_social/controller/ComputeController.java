package org.microserviceteam.microlink_social.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/social/compute")
public class ComputeController {

    // 接口 1：求平方
    @PostMapping("/square")
    public Double square(@RequestBody Double number) {
        System.out.println("Social服务：接收到数字 " + number + "，正在计算平方...");
        return number * number;
    }

    // 接口 2：取相反数
    @PostMapping("/negate")
    public Double negate(@RequestBody Double number) {
        System.out.println("Social服务：接收到数字 " + number + "，正在取相反数...");
        return -number;
    }
}
