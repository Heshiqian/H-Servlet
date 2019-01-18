package cn.heshiqian.framework.servlet.annotation.upload;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FileMapping {
    boolean multipleFileProcess() default false;
}
