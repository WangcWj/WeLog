package cn.wang.log.exceptions;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/12
 */
public class OpenFileException extends IllegalStateException {

    public OpenFileException(String s) {
        super(s);
    }

    public OpenFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
