package cn.heshiqian.framework.h.servlet.file;

public class FileConfig{

    //默认单个最大上传大小100MB
    protected long maxFileSize=1024*1024*100;
    //设置缓冲区的大小为10KB，如果不指定
    protected int sizeThreshold=10*1024;
    //设置文件编码默认UTF-8
    protected String headerEncoding="UTF-8";
    //最大上传总量默认100MB
    protected long sizeMax=1024*1024*100;

    public FileConfig() {
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getSizeThreshold() {
        return sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public String getHeaderEncoding() {
        return headerEncoding;
    }

    public void setHeaderEncoding(String headerEncoding) {
        this.headerEncoding = headerEncoding;
    }

    public long getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(long sizeMax) {
        this.sizeMax = sizeMax;
    }
}
