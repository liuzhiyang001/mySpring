package com.lzy.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/7/28 21:42
 * @ Description：
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LZYRequestParam {
    String value() default "";
}
