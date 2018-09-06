package cn.heshiqian.framework.h.servlet.handler;

import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public final class StaticFileHandle {


    private static StaticFileHandle staticFileHandle;
    private static CFLog cfLog=new CFLog(StaticFileHandle.class);
    private static ArrayList<String> staticFileList;

    public static StaticFileHandle newInstance(){
        if(staticFileHandle==null){
            staticFileHandle=new StaticFileHandle();
            cfLog.info("静态文件代理Handle已生成！地址："+staticFileHandle.toString());
            return staticFileHandle;
        }else
            return staticFileHandle;
    }

    public static void prepare(ArrayList<String> staticFileList){
        staticFileHandle.staticFileList=staticFileList;
    }

    public static boolean filter(HttpServletRequest request, HttpServletResponse response){
        //todo 这里应该吧所有的静态文件过滤掉
        String temp = request.getRequestURL().toString();
        String fileName=temp.substring(temp.lastIndexOf("/"),temp.length());
        temp=temp.substring(temp.indexOf("/", 10),temp.length());
        for(String s : staticFileList){
            if(s.equals(temp)){
                String contentType = request.getHeader("Accept").split(",")[0];
                ViewHandler.reSendStaticFile(response,temp,fileName,contentType, FrameworkMemoryStorage.staticFileLogSwitch);
                return true;
            }
        }
        //对于没有找到的静态内容，判断是不是文件，不是文件就返回没有接口
        String lastName = temp.substring(temp.lastIndexOf(".")+1, temp.length());
        if(FrameworkMemoryStorage.staticFileLogSwitch)
            cfLog.war("访问的文件不存在："+temp+"，后缀为："+lastName);

        if(HttpHelper.isMIME(lastName)) {
            try {
                ViewHandler.reSendErrorCode(response,404);
            } catch (IOException e) {
            }
            return true;
        }

        return false;
    }

}
