package cn.heshiqian.framework.h.servlet.factory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class SessionFactory {

    private HttpServletRequest request;
    private HttpServletResponse response;

    public SessionFactory(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest request(){
        return request;
    }

    public HttpServletResponse response(){
        return response;
    }

}
