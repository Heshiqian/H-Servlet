package cn.heshiqian.framework.h.servlet.tools;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public final class Tool {


    public static String FileFinder(File fFir,String fileName){

        File[] files = fFir.listFiles();
        for(int i=0;i<files.length;i++){
            if(files[i].isDirectory()){
                String rs = FileFinder(files[i], fileName);
                if(rs.equals("none"))
                    return "none";
                else
                    return rs;
            }
            if(fileName.equals(files[i].getName()))
                return files[i].getAbsolutePath();
            else
                return "none";
        }

        return "none";
    }

    public static String FileReadByUTF8(String path){

        int len;
        byte[] buff=new byte[2048];
        StringBuilder stringBuilder=new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(path));
            while ((len=fileInputStream.read(buff))!=-1){
                stringBuilder.append(new String(buff,0,len,"utf-8"));
            }
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return "文件读入失败！请检查是否有足够权限访问！";
    }

    public static String InjectKVMapToString(String old, HashMap<String,Object> map){
        String temp=old;
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            temp=temp.replace("{##"+next+"}",(String)map.get(next));
        }
        return temp;
    }

    public static String readInputStream(InputStream in) throws IOException {
        int len;
        byte[] buff=new byte[1024];
        String data="";
        while ((len=in.read(buff))!=-1){
            data+=new String(buff,0,len,"utf-8");
        }
        return data;
    }

    public static HashMap<String,String> jsonToHashMap(String json){
        HashMap<String,String> map=new HashMap<>();
        JSONObject jsonObject = JSONObject.fromObject(json);
        Iterator iterator = jsonObject.keySet().iterator();
        while (iterator.hasNext()){
            String next = (String) iterator.next();
            Object o = jsonObject.get(next);
            if(o instanceof Integer){
                map.put(next,String.valueOf(o));
            }else if(o instanceof String ){
                map.put(next,(String) o);
            }else if(o instanceof JSONObject){
                map.put(next,JSONObject.fromObject(o).toString());
            }else if(o instanceof JSONArray){
                map.put(next,JSONArray.fromObject(o).toString());
            }
        }
        return map;
    }

    public static HashMap<String, ?> jsonToList(String json){
        HashMap<String,?> listMap=new HashMap<>();
        JSONArray jsonArray = JSONArray.fromObject(json);
        for(int i=0;i<jsonArray.size();i++){
            Object o = jsonArray.get(i);
            if(o instanceof JSONObject);
        }


        return listMap;
    }


    public static ArrayList<String> listAllFile(String startPath){
        ArrayList<String> fileList=new ArrayList<>();
        File fDir=new File(startPath);

        File[] files = fDir.listFiles();
        for(File f:files){
            if(f.isDirectory()){
                if(f.getName().equals("WEB-INF")||f.getName().equals("META-INF"))
                    continue;
                fileList.addAll(listAllFile(f.getAbsolutePath()));
            }else {
                fileList.add(f.getAbsolutePath());
            }
        }
        return fileList;
    }

}
