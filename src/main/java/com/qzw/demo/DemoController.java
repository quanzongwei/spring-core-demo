package com.qzw.demo;

import com.qzw.annotaion.Autowire;
import com.qzw.annotaion.Controller;
import com.qzw.annotaion.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by BG388892 on 2019/4/29.
 */
@RequestMapping("/demo")
@Controller
public class DemoController {
    @Autowire
    private DemoService demoService;

    @RequestMapping("/sayHello")
    public void sayHello(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("DemoController的sayHello方法被调用");
        resp.setCharacterEncoding("UTF-8");
        resp.getOutputStream().write(demoService.sayHello().getBytes("UTF-8"));
    }
}
