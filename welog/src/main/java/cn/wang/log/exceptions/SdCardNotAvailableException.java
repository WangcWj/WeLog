package cn.wang.log.exceptions;

/**
 * Created to : SdCard不可用 就抛出异常。
 *
 * @author cc.wang
 * @date 2021/5/14
 */
public class SdCardNotAvailableException extends IllegalStateException {

    public SdCardNotAvailableException(String s) {
        super(s);
    }
}
