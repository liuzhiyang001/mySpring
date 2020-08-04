package com.lzy.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/7/28 21:40
 * @ Description：
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LZYRequestMapping {
    String value() default "";

}
