package cn.heshiqian.framework.servlet.annotation;


import cn.heshiqian.framework.servlet.pojo.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestUrl {
    String value();
    int method() default RequestMethod.GET;
}
