package cn.heshiqian.framework.servlet.factory;


import cn.heshiqian.framework.servlet.database.FrameworkMemoryStorage;
import cn.heshiqian.framework.servlet.exception.FileUploadException;
import cn.heshiqian.framework.servlet.file.FileConfig;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FileAcceptFactory {

    private FileConfig config;
    private byte[] buffer=new byte[1024*1024];

    private File target;

    public void setConfig(FileConfig config){
        this.config=config;
    }

    public void doAccept(HttpServletRequest request) throws FileUploadException, IOException, org.apache.commons.fileupload.FileUploadException {
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setSizeThreshold(config.getSizeThreshold());

        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        servletFileUpload.setFileSizeMax(config.getMaxFileSize());
        servletFileUpload.setSizeMax(config.getSizeMax());
        servletFileUpload.setHeaderEncoding(config.getHeaderEncoding());

        List<FileItem> fileItems = servletFileUpload.parseRequest(request);
        for(FileItem fileItem:fileItems){
            if(!fileItem.isFormField()){
                String name = fileItem.getName();
                String endName = name.substring(name.lastIndexOf("."));
                String uid = UUID.randomUUID().toString().replace("-", "");
                BufferedInputStream fbis = new BufferedInputStream(fileItem.getInputStream());
                target = new File(FrameworkMemoryStorage.uploadFileSaveF.getAbsolutePath() + "\\" + uid + endName);
                FileOutputStream fos = new FileOutputStream(target);
                int len;
                while ((len=fbis.read(buffer))!=-1){
                    fos.write(buffer,0,len);
                    fos.flush();
                }
                fos.close();
                break;
            }
        }

    }

    public File getTarget(){
        if(target==null||!target.exists())
            throw new FileUploadException("文件上传好像出了点问题");
        return target;
    }

}
