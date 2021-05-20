package cn.wang.log.core;

import java.io.File;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/11
 */
public class LogMsg implements Runnable {

    public String message;
    public int level;
    public int printMode;
    public String tag;
    public LogCenter log;
    public boolean isSync = false;

    public File logFile;
    public boolean logFileIsChange;
    public boolean inUse;

    LogMsg next;

    LogMsg() {
    }

    public void resetMessage(String msg) {
        this.message = msg;
    }

    public void resetLogFile(File file) {
        this.logFile = file;
    }

    public void logFileChange(boolean change) {
        this.logFileIsChange = change;
    }

    @Override
    public void run() {
        log.dispatchChain(this);
    }

    @Override
    public String toString() {
        return "LogMsg{" +
                "message='" + message + '\'' +
                ", level=" + level +
                ", printMode=" + printMode +
                '}';
    }
}
