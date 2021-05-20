package cn.wang.log.core;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/19
 */
public class LogMsgPool {

    private static final int MAX_POOL_SIZE = 5;
    private static LogMsg mPool;
    private static int mPoolSize = 0;

    public static LogMsg obtain() {
        if (mPool != null) {
            LogMsg m = mPool;
            mPool = m.next;
            m.next = null;
            mPoolSize--;
            return m;
        }
        return new LogMsg();
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
        if (mPoolSize < MAX_POOL_SIZE) {
            msg.next = mPool;
            mPool = msg;
            mPoolSize++;
        }
    }


}
