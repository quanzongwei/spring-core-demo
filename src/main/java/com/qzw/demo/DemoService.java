package com.qzw.demo;

import com.qzw.annotaion.Service;

/**
 * Created by BG388892 on 2019/4/29.
 */
@Service
public class DemoService {
    public String sayHello() {
        System.out.println("Service的sayHello方法被调用");
        return "Service 返回的数据";
    }
}
