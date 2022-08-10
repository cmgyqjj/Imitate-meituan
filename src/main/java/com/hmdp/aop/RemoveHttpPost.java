package com.hmdp.aop;
import java.lang.annotation.*;
/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoveHttpPost {
}