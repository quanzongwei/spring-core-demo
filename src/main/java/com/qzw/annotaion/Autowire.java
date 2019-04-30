package com.qzw.annotaion;

import java.lang.annotation.*;

/**
 * Created by BG388892 on 2019/4/29.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowire {
    String value() default "";
}
