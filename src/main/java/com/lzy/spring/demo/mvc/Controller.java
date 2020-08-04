package com.lzy.spring.demo.mvc;

import com.lzy.spring.demo.service.Service;
import com.lzy.spring.framework.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/7/28 21:32
 * @ Description：
 */
@LZYController
@LZYRequestMapping("/")
public class Controller {

    @LZYAutoWired("service")
    private Service service;

    @LZYRequestMapping("hello")
    public void hello(HttpServletRequest req, HttpServletResponse resp,@LZYRequestParam("name") String name){
        try {
            resp.getWriter().print(service.qurey(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
