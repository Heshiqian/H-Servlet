package cn.hsq.fw.test.service;

import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.servlet.annotation.*;
import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;
import cn.heshiqian.framework.h.servlet.pojo.VO;

import javax.servlet.http.Cookie;
import java.util.Arrays;

@Mapping
public class TestReqClass {

    private CFLog cfLog=new CFLog(TestReqClass.class);


    @NullReturn
//    @MapToFile //这个决定是否使用VO返回，VO的话，必须加这个，不加这个VO对象无效
    @RequestUrl(value = "/a") //设置访问Url
    /**
     * _@Cookies 是获取Cookie的注解
     * _@GetParam 是获取GET请求的值
     * _@TextString 是获取GET请求的原始字符串
     * _@NullReturn 是使函数返回为空时使用的，避免出现提示
     */
    public VO t(@Cookies Cookie[] cookie, @GetParam(key = "a") String a, Object t, @TextString String s){
        System.out.println(Arrays.toString(cookie));
        System.out.println(a);
        System.out.println(t);
        System.out.println(s);
        cfLog.info("aha!welcome!");
        VO vo = new VO();
        vo.openTemplate();
        vo.setTemplateFile("A.html");
        vo.put( "a","我放了个东西在这里");
        return vo;
    }


//    @ResponseBody
    @MapToFile
    @RequestUrl(value = "/b",method = RequestMethod.POST)
    public VO t2(@JSONString String json){
        System.out.println(json);
        VO vo = new VO();
        vo.openTemplate();
        vo.setTemplateFile("A.html");
        vo.put("a","我放了个东西在这里");

        return vo;
    }




}
