package cn.heshiqian.framework.servlet.pojo;

import java.util.HashMap;

public class VO {

    private boolean isTemplate=false;
    private HashMap<String,Object> map=new HashMap<>();
    private String templateFile;

    public VO() {
    }

    public void openTemplate(){
        isTemplate=true;
    }

    public void put(String key,Object val){
        synchronized (map){
            map.put(key,val);
        }
    }

    public void setTemplateFile(String filePath){
        if(isTemplate)
            templateFile=filePath;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public String getTemplateFile() {
        return templateFile;
    }
}
