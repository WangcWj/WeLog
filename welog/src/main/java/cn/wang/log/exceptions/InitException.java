package cn.wang.log.exceptions;

/**
 * Created to : 初始化异常时抛出该异常。
 *
 * @author cc.wang
 * @date 2021/5/12
 */
public class InitException extends IllegalStateException {

    public InitException(String s) {
        super(s);
    }

    public InitException(String message, Throwable cause) {
        super(message, cause);
    }
}
