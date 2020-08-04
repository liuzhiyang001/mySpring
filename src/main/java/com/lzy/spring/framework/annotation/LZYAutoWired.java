package com.lzy.spring.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ Author     ：lzy
 * @ Date       ：Created in  2020/7/28 21:40
 * @ Description：
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LZYAutoWired {
    String value() default "";
}
