package cn.wang.log.config;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class LogConfig {

    /**
     * 输出到File
     */
    public static final int PRINT_FILE = 32;

    /**
     * 输出到Logcat
     */
    public static final int PRINT_LOGCAT = 64;

    /**
     * 框架关闭标识。
     */
    public static final int CLOSE = 128;


    /**
     * 1m
     */
    public static final int M = 1024 * 1024;

    /**
     * 5kB
     */
    public static final int K_5 = 5 * 1024;

    /**
     * SDCard的最小可用大小。
     */
    public static final int MIN_SDCARD_SIZE = 50 * M;

    public static final long DAYS = 24 * 60 * 60 * 1000;

    /**
     * 默认删除天数。
     */
    public static final long DEFAULT_DELETE_DAY = 7 * DAYS;

    public static final int THREAD_POOL = 1;

    public static final int THREAD_WORKER = 2;

    public static final int IO = 1;

    public static final int NIO = 2;


}
