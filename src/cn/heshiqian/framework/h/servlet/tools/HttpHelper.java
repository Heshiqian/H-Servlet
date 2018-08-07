package cn.heshiqian.framework.h.servlet.tools;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class HttpHelper {

    private static final String ERROR_PAGE_HTML_1="<html>" +
            "<meta charset=\"utf-8\">" +
            "<body>" +
            "<div><h2 style=\"color:#a91a05\">出错啦！！！</h2></div>"+
            "<div>";
    private static final String ERROR_PAGE_HTML_2= "</div>" +
            "</body>" +
            "</html>";

    public static void sendErr(HttpServletResponse response,String why){
        send(response,ERROR_PAGE_HTML_1+why+ERROR_PAGE_HTML_2);
    }

    public static void sendNormal(HttpServletResponse response,String string){
        response.setContentType("text/html; charset=utf-8");
        send(response,string);
    }

    public static void sendJson(HttpServletResponse response,String string){
        response.setHeader("contentType","application/json");
        send(response,string);
    }

    private static void send(HttpServletResponse response,String string){
        try {
            PrintWriter writer = response.getWriter();
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
