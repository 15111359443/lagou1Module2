package com.lagou.study.annotations;

import java.lang.annotation.*;

/**
 * @Description controller 控制层注解类
 * @date 2021-03-29 10:17
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouStudyTransactional {
    String value() default "";
}
