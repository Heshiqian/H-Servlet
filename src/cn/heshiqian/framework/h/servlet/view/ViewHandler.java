package cn.heshiqian.framework.h.servlet.view;

import cn.heshiqian.framework.h.cflog.core.CFLog;

import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.pojo.VO;
import cn.heshiqian.framework.h.servlet.startup.ContextScanner;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.View;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public final class ViewHandler {

    private static CFLog cfLog=new CFLog(ViewHandler.class);

    private void analysis(Object rs, HttpServletResponse response, Cookie[] cookies, boolean isMapToFile, String fileName) {
        if (rs == null) {
            HttpHelper.sendErr(response, HServlet.NULL_RETURN_WARING);
            return;
        }

        if (isMapToFile) {
            if (rs instanceof String) {
                //todo 按照一个正常的文本返回
                HttpHelper.sendNormal(response, (String) rs);
            } else if (rs instanceof VO) {
                //todo 按照ViewObject返回，需要解析VO对象
                VO vo = (VO) rs;
                if (vo.isTemplate()) {
                    String templateFile = vo.getTemplateFile();
                    if ("".equals(templateFile)) {
                        HttpHelper.sendErr(response, HServlet.VO_FORMAT_ERROR);
                    } else {
                        File fDir = new File(ContextScanner.getContext().getRealPath(FrameworkMemoryStorage.staticFilePath));
                        String path = Tool.FileFinder.find(fDir, vo.getTemplateFile());
                        if("none".equals(path)){
                            HttpHelper.sendErr(response,HServlet.FILE_NOT_FIND_ERROR_1 + vo.getTemplateFile()+HServlet.FILE_NOT_FIND_ERROR_2);
                            cfLog.err(HServlet.FILE_NOT_FIND_ERROR_3+vo.getTemplateFile()+HServlet.FILE_NOT_FIND_ERROR_4);
                            cfLog.err(HServlet.FILE_NOT_FIND_ERROR_5+fDir.getAbsolutePath());
                        }
                        String s = Tool.FileReadByUTF8(path);
                        HashMap<String, Object> map = vo.getMap();
                        if (map.size() == 0) {
                            HttpHelper.sendNormal(response, s);
                            return;
                        }
                        String fullTemple = Tool.InjectKVMapToString(s, map);
                        HttpHelper.sendNormal(response, fullTemple);
                    }
                } else {
                    HashMap<String, Object> map = vo.getMap();
                    if (map.size() == 0) {
                        HttpHelper.sendNormal(response, "");
                        return;
                    }
                }

            } else {
                //预留

            }
        }else {
            //没有加注解的情况
            if (rs instanceof VO){
                HttpHelper.sendErr(response,HServlet.VO_RETURN_ERROR);
                return;
            }
            HttpHelper.sendNormal(response,rs.toString());
        }
    }

    public void analysis(Object rs, HttpServletResponse response, Cookie[] cookies, boolean isMapToFile) {
        analysis(rs, response, cookies, isMapToFile, "");
    }

    public void analysis(Object rs, HttpServletResponse response, Cookie[] cookies) {
        analysis(rs, response, cookies, false);
    }


    public void analyzeResBody(Object rs, HttpServletResponse response, Cookie[] cookies){
        //todo cookie好像都没有写，到时候再处理
        if(rs==null){
            HttpHelper.sendNormal(response,"null");
            return;
        }
        if(rs.getClass().isArray()){
            try {
                JSONArray jsonArray = JSONArray.fromObject(rs);
                HttpHelper.sendJson(response,jsonArray.toString());
            }catch (JSONException e){
                HttpHelper.sendErr(response,HServlet.JSON_FORMAT_ERROR+e.getMessage());
            }
        }else if (rs instanceof Collection<?>){
            try {
                JSONArray jsonArray = JSONArray.fromObject(rs);
                HttpHelper.sendJson(response,jsonArray.toString());
            }catch (JSONException e){
                HttpHelper.sendErr(response,HServlet.JSON_FORMAT_ERROR+e.getMessage());
            }
        }else {
            try {
                JSONObject jsonObject = JSONObject.fromObject(rs);
                HttpHelper.sendJson(response,jsonObject.toString());
            }catch (JSONException e){
                HttpHelper.sendErr(response,HServlet.JSON_FORMAT_ERROR+e.getMessage());
            }
        }
    }


    public static void reSendStaticFile(HttpServletResponse response,String fileURI,String fileName,String head,boolean printLog){
        if(printLog){
            cfLog.war(HServlet.STATIC_LOG_1+fileURI);
            cfLog.war(HServlet.STATIC_LOG_2+head);
        }
        String fileLastName=fileName.substring(fileName.lastIndexOf(".")+1);
        String fileURL=FrameworkMemoryStorage.staticFileDir+fileURI.substring(1);

        if(head.equals("*/*")){
            //不设置头部发送，全接受型
            if(fileLastName.equals("woff")||fileLastName.equals("otf")||fileLastName.equals("eot")||fileLastName.equals("svg")||fileLastName.equals("woff2")||fileLastName.equals("ttf")){
                //字体文件，二进制发送(流)
                HttpHelper.sendStream(response,Tool.FileReadByStream(fileURL));
            }else {
                HttpHelper.sendCustomTitle(response,"*/*",Tool.FileReadByUTF8(fileURL));
            }
            return;
        }

        if(head.contains("text")||head.contains("html")||head.contains("css")||head.contains("xml")){
            //发送文本类内容
            HttpHelper.sendCustomTitle(response,head,Tool.FileReadByUTF8(fileURL));
        } else if(head.contains("application")||head.contains("json")){
            //Json格式
            HttpHelper.sendJson(response,Tool.FileReadByUTF8(fileURL));
        } else if(head.contains("image")||head.contains("webp")||head.contains("video")){
            //发送流
            HttpHelper.sendStream(response,Tool.FileReadByStream(fileURL));
        }

    }

    public static void reSendCustomText(HttpServletResponse response,String s){
        HttpHelper.sendNormal(response,"<h1>"+s+"</h1>");
    }

    public static void reSendErrorCode(HttpServletResponse response,int code) throws IOException {
        response.sendError(code);
    }

}
