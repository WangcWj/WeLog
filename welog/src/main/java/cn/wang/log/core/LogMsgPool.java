package cn.wang.log.core;

import cn.wang.log.config.LogConfig;

/**
 * Created to : {@link LogMsg}对象的缓存池。
 *
 * @author cc.wang
 * @date 2021/5/19
 */
public class LogMsgPool {

    public static final Object lock = new Object();
    private static final int MAX_POOL_SIZE = 10;
    private static LogMsg mPool;
    private static int mPoolSize = 0;

    public static LogMsg obtain() {
        synchronized (lock) {
            if (mPool != null) {
                LogMsg m = mPool;
                mPool = m.next;
                m.next = null;
                mPoolSize--;
                return m;
            }
        }
        return new LogMsg();
    }

    public static LogMsg obtainCloseMsg(LogCenter log,String msg) {
        LogMsg obtain = obtain();
        obtain.printMode = LogConfig.CLOSE;
        obtain.inUse = true;
        obtain.message = msg;
        obtain.log = log;
        return obtain;
    }

    public static LogMsg obtain(LogCenter log, String message, String tag, int level, int printMode) {
        LogMsg obtain = obtain();
        obtain.message = message;
        obtain.level = level;
        obtain.printMode = printMode;
        obtain.log = log;
        obtain.tag = tag;
        obtain.inUse = true;
        return obtain;
    }

    /**
     * LogMsg使用完之后，要回收一下。
     *
     * @param msg
     */
    public static void recycler(LogMsg msg) {
        if (msg.inUse) {
            return;
        }
        msg.message = null;
        msg.level = -1;
        msg.printMode = -1;
        msg.tag = null;
        msg.log = null;
        msg.isSync = false;
        msg.logFile = null;
        msg.logFileIsChange = false;
        synchronized (lock) {
            if (mPoolSize < MAX_POOL_SIZE) {
                msg.next = mPool;
                mPool = msg;
                mPoolSize++;
            }
        }
    }

}
