package com.lzy.spring.demo.service.impl;

import com.lzy.spring.demo.service.Service;
import com.lzy.spring.framework.annotation.LZYService;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/8/4 15:40
 * @ Description：
 */
@LZYService("service")
public class ServiceImpl implements Service {

    public String qurey(String name){
        return "hello  "+name;
    }
}
