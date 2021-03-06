package cn.heshiqian.framework.servlet.tools;


import cn.heshiqian.framework.servlet.pojo.RequestMethod;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public final class Tool {

    public static class FileFinder{
        private static String fileFinderPath="none";
        public static String find(File file,String fileName){
            fileFinderPath="none";
            return _f(file,fileName);
        }
        private static String _f(File fFir,String fileName){
            File[] files = fFir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    _f(files[i],fileName);
                }
                if(files[i].isFile())
                    if (fileName.equals(files[i].getName())){
                        fileFinderPath = files[i].getAbsolutePath();
                    }
            }
            return fileFinderPath;
        }
    }
//    public static String FileFinder(File fFir,String fileName){
//        File[] files = fFir.listFiles();
//        for(int i=0;i<files.length;i++){
//            if(files[i].isDirectory()){
//                FileFinder(files[i], fileName);
//            }
//            if(fileName.equals(files[i].getName()))
//                fileFinderPath = files[i].getAbsolutePath();
//        }
//        return fileFinderPath;
//    }

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

    public static InputStream FileReadByStream(String path){

        File file = new File(path);
        if(file.exists()){
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("文件貌似没有权限访问！");
            }
        }else {
            throw new RuntimeException(new FileNotFoundException("找不到文件："+path));
        }
        return null;
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
        throw new UnsupportedOperationException();
//        Gson gson = new Gson();
//        HashMap<String,String> map=new HashMap<>();
//        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
//        jsonObject.get("").getAsString();
//

//        Iterator iterator = jsonObject.keySet().iterator();
//        while (iterator.hasNext()){
//            String next = (String) iterator.next();
//            Object o = jsonObject.get(next);
//            if(o instanceof Integer){
//                map.put(next,String.valueOf(o));
//            }else if(o instanceof String ){
//                map.put(next,(String) o);
//            }else if(o instanceof JSONObject){
//                map.put(next,JSONObject.fromObject(o).toString());
//            }else if(o instanceof JSONArray){
//                map.put(next,JSONArray.fromObject(o).toString());
//            }
//        }
//        return map;
    }

    public static HashMap<String, ?> jsonToList(String json){
        throw new UnsupportedOperationException();
//        HashMap<String,?> listMap=new HashMap<>();
//        JSONArray jsonArray = JSONArray.fromObject(json);
//        for(int i=0;i<jsonArray.size();i++){
//            Object o = jsonArray.get(i);
//            if(o instanceof JSONObject);
//        }


//        return listMap;
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

    public static void writeStreamToFile(InputStream stream, String name, String local) throws IOException{
        FileOutputStream fos = new FileOutputStream(local + "\\" + name);
        int len;
        byte[] buff=new byte[1024];
        while ((len=stream.read(buff))!=-1){
            fos.write(buff,0,len);
            fos.flush();
        }
        fos.close();
    }

    public static String requestMethodCodeVName(int code){
        switch (code){
            case RequestMethod.GET:
                return "GET";
            case RequestMethod.POST:
                return "POST";
            case RequestMethod.DELETE:
                return "DELETE";
            case RequestMethod.PUT:
                return "PUT";
        }
        return "";
    }

}
