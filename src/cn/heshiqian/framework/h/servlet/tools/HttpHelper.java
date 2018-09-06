package cn.heshiqian.framework.h.servlet.tools;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

public final class HttpHelper {

    private static final String ERROR_PAGE_HTML_1 = "<html>" +
            "<meta charset=\"utf-8\">" +
            "<body>" +
            "<div><h2 style=\"color:#a91a05\">出错啦！！！</h2></div>" +
            "<div>";
    private static final String ERROR_PAGE_HTML_2 = "</div>" +
            "</body>" +
            "</html>";

    public static void sendErr(HttpServletResponse response, String why) {
        send(response, ERROR_PAGE_HTML_1 + why + ERROR_PAGE_HTML_2);
    }

    public static void sendNormal(HttpServletResponse response, String string) {
        response.setContentType("text/html; charset=utf-8");
        send(response, string);
    }

    public static void sendJson(HttpServletResponse response, String string) {
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json");
        send(response, string);
    }

    public static void sendCustomTitle(HttpServletResponse response, String head, String string) {
        response.setHeader("Content-Type", head);
        response.setCharacterEncoding("utf-8");
        send(response, string);
    }

    public static void sendStream(HttpServletResponse response, InputStream inputStream) {
        SSend(response, inputStream);
    }

    private static void send(HttpServletResponse response, String string) {
        try {
            PrintWriter writer = response.getWriter();
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void SSend(HttpServletResponse response, InputStream inputStream) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        try {
            byte[] buffer=new byte[1024];
            int len;
            ServletOutputStream outputStream = response.getOutputStream();

            while ((len=bufferedInputStream.read(buffer))!=-1){
                outputStream.write(buffer,0,len);
            }

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean isMIME(String lastName){
        if(lastName.equals("html")||lastName.equals("txt")||lastName.equals("gif")||lastName.equals("jpg")||lastName.equals("png"))
            return true;
        if(lastName.equals("au")||lastName.equals("midi")||lastName.equals("mpg")||lastName.equals("mpeg")||lastName.equals("avi"))
            return true;
        if(lastName.equals("mp4")||lastName.equals("mp3")||lastName.equals("gz")||lastName.equals("zip")||lastName.equals("tar"))
            return true;
        if(lastName.equals("css")||lastName.equals("js")||lastName.equals("woff")||lastName.equals("woff2")||lastName.equals("font"))
            return true;
        if(lastName.equals("jsp")||lastName.equals("xml")||lastName.equals("json")||lastName.equals("svg")||lastName.equals("eot"))
            return true;
        if(lastName.equals("otf")||lastName.equals("shtml")||lastName.equals("json")||lastName.equals("svg")||lastName.equals("eot"))
            return true;

        return false;
    }
}
