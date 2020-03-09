//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alcatelsbell.cdcp.web.common.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DicItem {

    String desc();
    String code();
    String color() default "";
    int value() default  -1;
}
