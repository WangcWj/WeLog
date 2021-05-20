package cn.wang.log.core;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cn.wang.log.config.LogConfig;
import cn.wang.log.dispatchers.Dispatcher;
import cn.wang.log.dispatchers.ThreadPoolDispatcher;
import cn.wang.log.loginterceptors.AndroidInterceptor;
import cn.wang.log.loginterceptors.EncryptionInterceptor;
import cn.wang.log.loginterceptors.EndOfInterceptor;
import cn.wang.log.loginterceptors.FileConfigurationInterceptor;
import cn.wang.log.loginterceptors.WriterFileInterceptor;
import cn.wang.log.loginterceptors.FormatLogMessageInterceptor;
import cn.wang.log.loginterceptors.WeLogInterceptor;
import cn.wang.log.loginterceptors.RealInterceptorChain;
import cn.wang.log.config.LogLevel;

/**
 * Created to :
 * 1.组装日志消息
 * 2.日志格式化。
 * 3.默认一天写一个文件。
 * 4.默认文件超过设置的大小，重新起新文件，新文件的后缀为“$$1”
 * 5.只保留7天文件，文件的最后修改时间超过7天就删除掉。
 *
 * @author cc.wang
 * @date 2021/5/11
 */
public class LogCenter {

    private String mLogFilePath;
    private String mLogTag;
    private int mLogFileSize;
    private long mDeleteDay;
    private List<WeLogInterceptor> mWeLogInterceptors;
    private Dispatcher mDispatcher;
    private RealInterceptorChain mRootChain;

    private LogCenter(Builder builder) {
        mLogFilePath = builder.logFilePath;
        mLogFileSize = builder.logFileSize;
        mLogTag = builder.logTag;
        mWeLogInterceptors = builder.weLogInterceptors;
        mDispatcher = builder.dispatcher;
    }

    public String getLogFilePath() {
        return mLogFilePath;
    }

    public int getLogFileSize() {
        return mLogFileSize;
    }

    public long getDeleteDay() {
        return mDeleteDay;
    }

    public void d(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT, LogLevel.DEBUG, msg);
    }

    public void e(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT, LogLevel.ERROR, msg);
    }

    public void w(String msg) {
        dispatcher(LogConfig.PRINT_FILE, LogLevel.DEBUG, msg);
    }

    public void dw(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT | LogConfig.PRINT_FILE, LogLevel.DEBUG, msg);
    }

    public void ew(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT | LogConfig.PRINT_FILE, LogLevel.ERROR, msg);
    }

    private void dispatcher(int printMode, int logLevel, String msg) {
        mDispatcher.dispatch(LogMsgPool.obtain(this, msg, mLogTag, logLevel, printMode));
    }

    public void dispatchChain(LogMsg message) {
        if(mRootChain == null) {
            mRootChain = new RealInterceptorChain(mWeLogInterceptors, 0);
        }
        try {
            LogMsg process = mRootChain.process(message);
            process.inUse = false;
            LogMsgPool.recycler(process);
        } catch (Exception e) {
            //处理请求过程中产生的异常。
        }
    }

    public void close() {

    }

    public static class Builder {
        private String logFilePath;
        private String logTag;
        private int logFileSize;
        private long deleteDay;
        private Context applicationContext;
        private List<WeLogInterceptor> weLogInterceptors = new ArrayList<>();
        private Dispatcher dispatcher;

        public Builder(Context applicationContext) {
            checkParam(applicationContext, "LogCenter$Builder: applicationContext is null !");
            this.applicationContext = applicationContext;
        }

        /**
         * 设置日志文件的文件夹，比如：
         * getExternalCacheDir().getAbsolutePath()+ File.separator+"WeLog"
         *
         * @param logFilePath
         */
        public Builder setLogFilePath(String logFilePath) {
            checkParam(logFilePath, "Builder.setLogFilePath() : logFilePath is empty !");
            this.logFilePath = logFilePath;
            return this;
        }

        /**
         * 设置日志文件的大小。
         *
         * @param logFileSize 默认是1024的倍数。
         */
        public Builder setLogFileSize(int logFileSize) {
            if (logFileSize <= 0) {
                throw new IllegalArgumentException("Builder.setLogFileSize() : logFileSize is <= 0 !");
            }
            this.logFileSize = (logFileSize / 1024) * 1024;
            return this;
        }

        /**
         * 设置日志文件的保存时间。
         *
         * @param day
         * @return
         */
        public Builder setLogFileSaveDays(int day) {
            if (day <= 0) {
                throw new IllegalArgumentException("Builder.setLogFileSaveDays() : day is <= 0 !");
            }
            this.deleteDay = day;
            return this;
        }

        /**
         * 设置日志的Tag。
         *
         * @param logTag
         */
        public Builder setLogTag(String logTag) {
            checkParam(logTag, "Builder.setLogTag() : mLogTag is empty !");
            this.logTag = logTag;
            return this;
        }

        LogCenter build() {
            if (TextUtils.isEmpty(logTag)) {
                logTag = "cc.wang";
            }
            if (TextUtils.isEmpty(logFilePath)) {
                logFilePath = applicationContext.getExternalCacheDir().getAbsolutePath() + File.separator + "WeLog";
            }
            if (logFileSize <= 0) {
                logFileSize = LogConfig.M;
            }
            if(deleteDay <= 0){
                deleteDay = LogConfig.DEFAULT_DELETE_DAY;
            }
            weLogInterceptors.add(new AndroidInterceptor());
            weLogInterceptors.add(new FormatLogMessageInterceptor());
            weLogInterceptors.add(new EncryptionInterceptor());
            weLogInterceptors.add(new FileConfigurationInterceptor());
            weLogInterceptors.add(new WriterFileInterceptor());
            weLogInterceptors.add(new EndOfInterceptor());
            if (dispatcher == null) {
                dispatcher = new ThreadPoolDispatcher();
            }
            return new LogCenter(this);
        }

        private void checkParam(Object value, String msg) {
            if (null == value) {
                throw new IllegalArgumentException(msg);
            }
        }
    }
}
