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
}
