package cn.wang.log.core;

/**
 * Created to :
 *
 * <p>
 * 1.采用Builder方式来建造。
 * 2.日志的处理应该采用责任链的模式，可以仿照OkHttp。因为日志信息可能需要要经过很多的步骤，
 * 比如说日志格式化、系统打印日志、日志加密、日志写入文件等，这样的话还是采用责任链模式来说比较解耦，
 * 另外如果要替换某个步骤的实现也很简单。
 * 责任链设计模式的实现：
 * 1.使用单向链表的模式，组合简单高效，但事件总是单向传递，无法形成回路。
 * 2.
 * <p>
 * </>
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class WeLog {

    private static boolean initialized;
    private static LogCenter log;

    private WeLog() {
    }

    public static void init(LogCenter.Builder builder) {
        if (initialized) {
            return;
        }
        try {
            log = builder.build();
            initialized = true;
        } catch (Exception e) {
            initialized = false;
        }
    }

    public static void unInit(){
        if(log != null){
            log.close();
        }
    }

    public static void e(String msg) {
        assertInitialization(msg);
        log.e(msg);
    }

    public static void d(String msg) {
        assertInitialization(msg);
        log.d(msg);
    }

    public static void w(String msg) {
        assertInitialization(msg);
        log.w(msg);
    }

    public static void dw(String msg) {
        assertInitialization(msg);
        log.dw(msg);
    }

    public static void ew(String msg) {
        assertInitialization(msg);
        log.ew(msg);
    }

    public static void assertInitialization(String msg) {
        if (!initialized) {
            throw new IllegalStateException("请先调用WeLog.init()方法！");
        }
        if (null == msg) {
            throw new IllegalArgumentException("The log message is null !");
        }
    }

}
