package cn.heshiqian.framework.h.servlet.factory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileExtraProcessor {

    void beforeAccept(HttpServletRequest request);

    void afterAccept(HttpServletResponse response);

}
