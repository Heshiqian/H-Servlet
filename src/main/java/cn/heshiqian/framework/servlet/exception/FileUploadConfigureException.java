package cn.heshiqian.framework.servlet.exception;

public class FileUploadConfigureException extends RuntimeException {

    public FileUploadConfigureException(String message) {
        super(message);
    }

    public FileUploadConfigureException(String message, Throwable cause) {
        super(message, cause);
    }
}
