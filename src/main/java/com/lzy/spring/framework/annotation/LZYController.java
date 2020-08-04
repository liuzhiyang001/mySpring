package com.lzy.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/7/28 21:36
 * @ Description：
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LZYController {
    String value() default "";
}
