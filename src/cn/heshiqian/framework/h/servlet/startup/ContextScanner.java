package cn.heshiqian.framework.h.servlet.startup;

import cn.heshiqian.framework.h.servlet.database.FrameworkMemoryStorage;

import javax.servlet.ServletContext;

public final class ContextScanner {

    private static ServletContext context;
    private static ContextScanner contextScanner;

    private ContextScanner(){
    }

    public static ContextScanner newInstance(){
        if(contextScanner==null){
            contextScanner=new ContextScanner();
            return contextScanner;
        }else{
            return contextScanner;
        }
    }

    public static void saveContext(ServletContext context){
        contextScanner.setContext(context);
        FrameworkMemoryStorage.context=context;
    }

    public static ServletContext getContext(){
        return contextScanner.gContext();
    }

    private void setContext(ServletContext context) {
        ContextScanner.context = context;
    }
    private ServletContext gContext() {
        return context;
    }
}
