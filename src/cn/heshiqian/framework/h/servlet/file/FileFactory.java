package cn.heshiqian.framework.h.servlet.file;

import cn.heshiqian.framework.h.cflog.core.CFLog;
import cn.heshiqian.framework.h.cflog.core.Logger;
import cn.heshiqian.framework.h.servlet.exception.FileUploadConfigureException;
import cn.heshiqian.framework.h.servlet.exception.FileUploadException;
import cn.heshiqian.framework.h.servlet.factory.FileAcceptFactory;
import cn.heshiqian.framework.h.servlet.factory.FileExtraProcessor;
import cn.heshiqian.framework.h.servlet.tools.HttpHelper;
import com.sun.istack.internal.Nullable;
import org.apache.commons.fileupload.FileUploadBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public final class FileFactory{

    private static final String RESEND_TEXT="ok";

    private Logger logger= CFLog.logger(FileFactory.class);
    private FileExtraProcessor fileExtraProcessor;
    private FileConfig config;

    private File doneFile;

    //反射注入这三个变量，直接new会出错
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FileAcceptFactory fileAcceptFactory;

    private boolean ConfigFlag=false;
    private boolean CompleteFlag=false;
    private boolean WasRelease=true;

    /**
     * 默认设置接收文件
     */
    public void accept(){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        accept0();
    }

    /**
     * 通过一个接口，可以在接收前和接收后进行额外处理
     * 但是最好不要在这里面去把response提交了，不然后面的ViewHandle不会提交
     * @param fileExtraProcessor 额外接口
     */
    public void accept(FileExtraProcessor fileExtraProcessor){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        this.fileExtraProcessor=fileExtraProcessor;
        accept0();
    }

    /**
     * 私有方法，接收
     */
    private void accept0(){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        if(config==null&&!ConfigFlag){
            logger.err("你还没有执行config");
            throw new FileUploadConfigureException("你还没有执行config");
        }
        if(fileExtraProcessor!=null){
            fileExtraProcessor.beforeAccept(request);
        }
        fileAcceptFactory.setConfig(config);
        try {
            fileAcceptFactory.doAccept(request);
            doneFile=fileAcceptFactory.getTarget();
            if(doneFile!=null)
                CompleteFlag=true;
        } catch (FileUploadBase.FileSizeLimitExceededException e){
            HttpHelper.sendErr(response,"单个文件超出设置的最大大小!");
            return;
        } catch (FileUploadBase.SizeLimitExceededException e){
            HttpHelper.sendErr(response,"总文件大小超出大小!");
            return;
        } catch (org.apache.commons.fileupload.FileUploadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fileExtraProcessor!=null){
            fileExtraProcessor.afterAccept(response);
        }
    }

    /**
     * 获取接收的文件，失败为Null
     * @return 上传成功的文件
     */
    @Nullable
    public File getFile(){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        if(!CompleteFlag)
            throw new FileUploadException("文件还未接收，请先执行accpet函数");
        if(doneFile==null||!doneFile.exists())
            throw new FileUploadException("文件接收为空，未知错误");
        return doneFile;
    }

    /**
     * 获取文件的输入流
     * @return FileInputStream
     * @throws IOException 文件占用或不存在抛出
     */
    public InputStream getInputStream() throws IOException {
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        if(!CompleteFlag)
            throw new FileUploadException("文件还未接收，请先执行accpet函数");
        if(doneFile==null||!doneFile.exists())
            throw new FileUploadException("文件接收为空，未知错误");
        return new FileInputStream(doneFile);
    }

    /**
     * 删除接收的这个文件
     */
    public void deleteThisFile(){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        if(doneFile!=null&&doneFile.exists()) {
            WasRelease=false;
            boolean delete = doneFile.delete();
            if(!delete)
                logger.print("文件:"+doneFile.getAbsolutePath()+"删除失败！不影响程序运行.");
        }
    }

    /**
     * 执行配置
     * @param fileConfig 配置Pojo
     */
    public void config(FileConfig fileConfig){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        ConfigFlag=true;
        this.config=fileConfig;
    }

    /**
     * 释放线程阻塞，使用在需对文件进行长时间加载时使用
     * @param something 长时间任务
     * @return 创建的线程
     */
    public Thread release(Runnable something){
        if(!WasRelease)
            throw new FileUploadException("已执行释放，不可操作");
        else
            WasRelease=false;
        Thread task = new Thread(something);
        task.start();
        return task;
    }

}
