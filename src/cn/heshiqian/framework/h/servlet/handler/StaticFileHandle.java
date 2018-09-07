package cn.heshiqian.framework.h.servlet.handler;

import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import cn.heshiqian.framework.h.servlet.view.ViewHandler;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

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
    private static CFLog cfLog=new CFLog(StaticFileHandle.class);
    private static ArrayList<String> staticFileList;

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
                JSONArray jsonArray = JSONArray.fromObject(confJson.trim());
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    diyMapping.put(jsonObject.getString("type"),jsonObject.getString("value"));
                }
                cfLog.info(HServlet.SFH_INFO_2);
                cfLog.print(diyMapping.toString());
            }catch (JSONException e){
                e.printStackTrace();
                cfLog.err(HServlet.SFH_ERROR_2);
            }
        }
    }

    public static boolean filter(HttpServletRequest request, HttpServletResponse response){
        String temp = request.getRequestURL().toString();
        String contentType = request.getHeader("Accept").split(",")[0];
        //关闭模式
        if(FrameworkMemoryStorage.filterType==HServlet.FILER_TYPE_OFF)
            return false;

        //自动模式
        if(FrameworkMemoryStorage.filterType==HServlet.FILER_TYPE_AUTO) {
            String fileName = temp.substring(temp.lastIndexOf("/"));
            temp = temp.substring(temp.indexOf("/", 10));
            for (String s : staticFileList) {
                if (s.equals(temp)) {
                    ViewHandler.reSendStaticFile(response, temp, fileName, contentType, FrameworkMemoryStorage.staticFileLogSwitch);
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
