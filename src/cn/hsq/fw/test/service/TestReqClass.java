package cn.hsq.fw.test.service;

import cn.heshiqian.framework.h.cflog.core.*;
import cn.heshiqian.framework.h.cflog.core.Logger;
import cn.heshiqian.framework.h.servlet.annotation.*;
import cn.heshiqian.framework.h.servlet.annotation.upload.FileMapping;
import cn.heshiqian.framework.h.servlet.factory.FileExtraProcessor;
import cn.heshiqian.framework.h.servlet.factory.SessionFactory;
import cn.heshiqian.framework.h.servlet.file.FileFactory;
import cn.heshiqian.framework.h.servlet.file.FileConfig;
import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;
import cn.heshiqian.framework.h.servlet.pojo.VO;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;

@Mapping
public class TestReqClass {

    private Logger cfLog=CFLog.logger(TestReqClass.class);

    @NullReturn
//    @MapToFile //这个决定是否使用VO返回，VO的话，必须加这个，不加这个VO对象无效
    @RequestUrl("/a") //设置访问Url
    /**
     * _@Cookies 是获取Cookie的注解
     * _@GetParam 是获取GET请求的值
     * _@TextString 是获取GET请求的原始字符串
     * _@NullReturn 是使函数返回为空时使用的，避免出现提示
     */
    public void t(@Cookies Cookie[] cookie, @GetParam(key = "a") String a, Object t, @TextString String s){
        System.out.println(Arrays.toString(cookie));
        System.out.println(a);
        System.out.println(t);
        System.out.println(s);
        cfLog.info("aha!welcome!");
        VO vo = new VO();
        vo.openTemplate();
        vo.setTemplateFile("A.html");
        vo.put( "a","我放了个东西在这里");
    }


//    @ResponseBody
//    @MapToFile
    @RequestUrl(value = "/b",method = RequestMethod.POST)
    public void t2(@JSONString String json){
        System.out.println(json);
    }

    @FileMapping
    @RequestUrl(value = "/f",method = RequestMethod.POST)
    public String t3(FileFactory fileFactory){
        //新建配置类
        FileConfig fileConfig = new FileConfig();
        fileConfig.setHeaderEncoding("UTF-8");
        //把这个配置导入
        fileFactory.config(fileConfig);

        //这里执行接收文件，可能阻塞
        //也可以直接空参，这样做可以在接收前和接收后进行额外处理
        //比如权限控制就可以在这里做了
        fileFactory.accept(new FileExtraProcessor() {
            @Override
            public void beforeAccept(HttpServletRequest request) {

            }

            @Override
            public void afterAccept(HttpServletResponse response) {

            }
        });

        fileFactory.deleteThisFile();

        //释放资源，可以在里面写入一个阻塞方法，后台执行
        fileFactory.release(new Runnable() {
            @Override
            public void run() {
                //time task
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        return "{'code':1,'str':'成功上传'}";
    }

    @RequestUrl(value = "/t3")
    public void t3(@Session SessionFactory a){
        System.out.println(a.toString());
    }

    @RequestUrl(value = "/t4")
    private void t5(@TextString String text){
        System.out.println(text);
    }

    @RequestUrl(value = "/t5",method = RequestMethod.DELETE)
    private void t6(){

    }

}
