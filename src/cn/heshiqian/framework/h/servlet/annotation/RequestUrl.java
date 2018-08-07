package cn.heshiqian.framework.h.servlet.annotation;

import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestUrl {
    String value() default "";
    int method() default RequestMethod.GET;
}
