package cn.heshiqian.framework.h.servlet.handler;

import cn.heshiqian.framework.h.cflog.core.*;
import cn.heshiqian.framework.h.servlet.database.HServlet;
import cn.heshiqian.framework.h.servlet.pojo.RequestMethod;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import cn.heshiqian.framework.h.servlet.tools.Tool;
import net.sf.json.JSONException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

public final class ServletReqHandler {

    private static final String ICON_FILE_NAME="favicon.ico";


    private static Class mainServletClass;
    private static Logger cfLog=CFLog.logger(ServletReqHandler.class);
    private static CenterHandle centerHandle=new CenterHandle();

    public ServletReqHandler(Class main){
        this.mainServletClass=main;
    }

    private ServletReqHandler() {
    }

    public void GetHandler(HttpServletRequest request, HttpServletResponse response){
        //这里开始都是分割请求地址
        String serverPort = String.valueOf(request.getServerPort());
        String requestURL = request.getRequestURL().toString();
        requestURL=requestURL.substring(requestURL.indexOf(serverPort)+serverPort.length(),requestURL.length());

        //去除浏览器的favicon.ico请求
        if(checkIconFile(requestURL)) return;
        //单'/'处理首页
        if(requestURL.equals("/")) return;

        cfLog.info("path:"+requestURL);
        //解析出GET请求的参数key和值
        HashMap<String,String> keyMap=new HashMap<>();
        Enumeration<String> keys = request.getParameterNames();
        while (keys.hasMoreElements()){
            String key = keys.nextElement();
            keyMap.put(key,request.getParameter(key));
        }

        //交由中心Handle处理
        centerHandle.distributor(RequestMethod.GET,requestURL,request,response,request.getCookies(),keyMap);
    }

    public void PostHandler(HttpServletRequest request, HttpServletResponse response){
        //这里开始都是分割请求地址
        String serverPort = String.valueOf(request.getServerPort());
        String requestURL = request.getRequestURL().toString();
        requestURL=requestURL.substring(requestURL.indexOf(serverPort)+serverPort.length(),requestURL.length());
        cfLog.info("path:"+requestURL);

        //这里区分是文件还是正常的post提交
        boolean multipartContent = ServletFileUpload.isMultipartContent(request);
        if(multipartContent){
            //按照文件的方式处理

        }else {
            //解析出POST请求的参数key和值
            HashMap<String,String> keyMap=new HashMap<>();
            Enumeration<String> keys = request.getParameterNames();
            while (keys.hasMoreElements()){
                String key = keys.nextElement();
                keyMap.put(key,request.getParameter(key));
            }
            //表单验证
            if(keyMap.size()==0){
                //证明不是表单提交的，应该是json字符串，解析JSON字符串
                try {
                    String json = Tool.readInputStream(request.getInputStream());

                    if(json.equals("")){
                        HttpHelper.sendErr(response,HServlet.SQH_ERROR_1);
                        return;
                    }
                    if(json.indexOf("{")==0&&json.lastIndexOf("}")==json.length()-1){
                        //证明这个应该是JSONObject对象
                        HashMap<String, String> map = Tool.jsonToHashMap(json);
                        keyMap.putAll(map);
                        keyMap.put(HServlet.SYS_CONSTANT_KEY,json);
                        centerHandle.distributor(RequestMethod.POST,requestURL,request,response,request.getCookies(),keyMap);
                        return;
                    }
                    if(json.indexOf("[")==0&&json.lastIndexOf("]")==json.length()-1){
                        //证明这是个数组JSONArray对象
                        //todo 不完整，需完善
//                        HashMap<String, ?> listMap = Tool.jsonToList(json);
//                        keyMap.putAll(listMap);
//                        centerHandle.distributor(RequestMethod.POST,requestURL,request,response,request.getCookies(),keyMap);
                        HttpHelper.sendErr(response,HServlet.SQH_ERROR_UNCODE);
                        return;
                    }
                    throw new JSONException(HServlet.SQH_ERROR_2);
                } catch (IOException e) {
                    HttpHelper.sendErr(response,HServlet.SQH_ERROR_3);
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkIconFile(String url){
        return url.contains(ICON_FILE_NAME);
    }
}
