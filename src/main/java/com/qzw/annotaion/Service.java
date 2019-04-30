package com.qzw.annotaion;

import java.lang.annotation.*;

/**
 * Created by BG388892 on 2019/4/29.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    String value() default "";
}
