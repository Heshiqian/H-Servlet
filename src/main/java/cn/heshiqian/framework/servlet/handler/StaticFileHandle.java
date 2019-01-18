package cn.heshiqian.framework.servlet.handler;

import cn.heshiqian.framework.h.cflog.core.*;
import cn.heshiqian.framework.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.servlet.database.HServlet;
import cn.heshiqian.framework.servlet.tools.HttpHelper;
import cn.heshiqian.framework.servlet.view.ViewHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;

public final class StaticFileHandle {

    private static IdentityHashMap<String,String> diyMapping=new IdentityHashMap<>();
    private static StaticFileHandle staticFileHandle;
    private static Logger cfLog=CFLog.logger(StaticFileHandle.class);
    private static ArrayList<String> staticFileList;
    private static Gson gson=new Gson();

    public static StaticFileHandle newInstance(){
        if(staticFileHandle==null){
            staticFileHandle=new StaticFileHandle();
            cfLog.info(HServlet.SFH_INFO_1+staticFileHandle.toString());
            return staticFileHandle;
        }else
            return staticFileHandle;
    }

    public static void prepare(ArrayList<String> staticFileList){
        StaticFileHandle.staticFileList =staticFileList;
        if(FrameworkMemoryStorage.filterType == HServlet.FILER_TYPE_CUSTOM){
            String confJson=FrameworkMemoryStorage.filterCustomContent;
            if(confJson==null||confJson.equals("")){
                cfLog.err(HServlet.SFH_ERROR_1);
                return;
            }
            try{
                JsonArray jsonArray = gson.fromJson(confJson.trim(),JsonArray.class);
                for(int i=0;i<jsonArray.size();i++){
                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                    diyMapping.put(jsonObject.get("type").getAsString(),jsonObject.get("value").getAsString());
                }
                cfLog.info(HServlet.SFH_INFO_2);
                cfLog.print(diyMapping.toString());
            }catch (JsonSyntaxException e){
                e.printStackTrace();
                cfLog.err(HServlet.SFH_ERROR_2);
            }
        }
    }

    public static boolean filter(HttpServletRequest request, HttpServletResponse response){
        String temp = request.getRequestURL().toString();
        String accept = request.getHeader("Accept");
        if (accept==null){
            cfLog.war("未知请求，无Accept信息头，可能会处理出错");
            return false;
        }
        String contentType = accept.split(",")[0];
        //关闭模式
        if(FrameworkMemoryStorage.filterType==HServlet.FILER_TYPE_OFF)
            return false;

        //自动模式
        if(FrameworkMemoryStorage.filterType==HServlet.FILER_TYPE_AUTO) {
            String fileName = temp.substring(temp.lastIndexOf("/"));
            temp = temp.substring(temp.indexOf("/", 10));
            for (String s : staticFileList) {
                if ((FrameworkMemoryStorage.contextPath+s).equals(temp)) {
                    ViewHandler.reSendStaticFile(response, temp.replace(FrameworkMemoryStorage.contextPath,""), fileName, contentType, FrameworkMemoryStorage.staticFileLogSwitch);
                    return true;
                }
            }
        }

        //自定义模式
        if(FrameworkMemoryStorage.filterType==HServlet.FILER_TYPE_CUSTOM){
            String path = temp.substring(temp.indexOf(FrameworkMemoryStorage.ServerPort) + FrameworkMemoryStorage.ServerPort.length());
            int i = path.lastIndexOf(".");
            if(i==-1)
                return false;
            String endName = path.substring(i);
            Iterator<String> iterator = diyMapping.keySet().iterator();
            while (iterator.hasNext()){
                String key = iterator.next();
                String val = diyMapping.get(key);
                if("address".equals(key)){
                    //按照路径解析
                    if(path.indexOf(val)==0){
                        String fileName = path.substring(path.lastIndexOf("/"));
                        File file = new File(FrameworkMemoryStorage.staticFileDir + val);
                        if(file.exists()&&file.isDirectory()) {
                            if(FrameworkMemoryStorage.staticFileLogSwitch)
                                cfLog.print(HServlet.SFH_INFO_3+path+HServlet.SFH_INFO_3_1+val);
                            temp = temp.substring(temp.indexOf("/", 10));
                            ViewHandler.reSendStaticFile(response,temp,fileName,contentType,FrameworkMemoryStorage.staticFileLogSwitch);
                            return true;
                        }
                    }
                }else if("extensions".equals(key)){
                    //按照后缀解析
                    String fileName = path.substring(path.lastIndexOf("/")+1);
                    String endd = val.substring(1);
                    if (endName.equals(endd)){
                        if(FrameworkMemoryStorage.staticFileLogSwitch)
                            cfLog.print(HServlet.SFH_INFO_4+fileName+HServlet.SFH_INFO_3_1+val);
                        temp = temp.substring(temp.indexOf("/", 10));
                        ViewHandler.reSendStaticFile(response,temp,fileName,contentType,FrameworkMemoryStorage.staticFileLogSwitch);
                        return true;
                    }
                }
            }
            cfLog.war(HServlet.SFH_INFO_5+path+HServlet.SFH_INFO_6);
        }



        //对于没有找到的静态内容，判断是不是文件，不是文件就返回没有接口
        String lastName = temp.substring(temp.lastIndexOf(".")+1);
        if(FrameworkMemoryStorage.staticFileLogSwitch)
            cfLog.war(HServlet.SFH_INFO_7+temp+HServlet.SFH_INFO_7_1+lastName);
        if(HttpHelper.isMIME(lastName)) {
            try {
                ViewHandler.reSendErrorCode(response,404);
            } catch (IOException e) {
            }
            return true;
        }
        //不是静态文件
        return false;
    }

}
