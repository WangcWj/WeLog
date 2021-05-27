package cn.wang.log.core;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.wang.log.config.LogConfig;
import cn.wang.log.dispatchers.Dispatcher;
import cn.wang.log.dispatchers.DispatcherFactory;
import cn.wang.log.loginterceptors.AndroidInterceptor;
import cn.wang.log.loginterceptors.EncryptionInterceptor;
import cn.wang.log.loginterceptors.EndOfInterceptor;
import cn.wang.log.loginterceptors.FileConfigurationInterceptor;
import cn.wang.log.loginterceptors.IOFileInterceptor;
import cn.wang.log.loginterceptors.NIOFileInterceptor;
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

    private static final String TAG = "LogCenter";

    private String mLogFilePath;
    private String mLogTag;
    private int mLogFileSize;
    private long mDeleteDay;
    private int mIoType;
    private List<WeLogInterceptor> mWeLogInterceptors;
    private Dispatcher mDispatcher;
    private RealInterceptorChain mRootChain;

    private LogCenter(Builder builder) {
        mLogFilePath = builder.logFilePath;
        mLogFileSize = builder.logFileSize;
        mLogTag = builder.logTag;
        mWeLogInterceptors = builder.weLogInterceptors;
        mDispatcher = builder.dispatcher;
        mDeleteDay = builder.deleteDay;
    }

    public int getIoType() {
        return mIoType;
    }

    /**
     * 获取日志文件的父文件夹路径。
     */
    public String getLogFilePath() {
        return mLogFilePath;
    }

    /**
     * 获取日志文件的最大大小，超过大小将重新生成新的日志。
     */
    public int getLogFileSize() {
        return mLogFileSize;
    }

    /**
     * 获取过期删除日志文件的时间。
     */
    public long getDeleteDay() {
        return mDeleteDay;
    }

    /**
     * 同Android中的Log.d,不输出到日志文件。
     *
     * @param msg 日志
     */
    public void d(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT, LogLevel.DEBUG, msg);
    }

    /**
     * 同Android中的Log.e,不输出到日志文件。
     *
     * @param msg 日志
     */
    public void e(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT, LogLevel.ERROR, msg);
    }

    /**
     * 同Android中的Log.w,不输出到日志文件。
     *
     * @param msg 日志
     */
    public void w(String msg) {
        dispatcher(LogConfig.PRINT_FILE, LogLevel.DEBUG, msg);
    }

    /**
     * 同Android中的Log.d且输出到日志文件。
     *
     * @param msg 日志
     */
    public void dw(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT | LogConfig.PRINT_FILE, LogLevel.DEBUG, msg);
    }

    /**
     * 同Android中的Log.e且输出到日志文件。
     *
     * @param msg 日志
     */
    public void ew(String msg) {
        dispatcher(LogConfig.PRINT_LOGCAT | LogConfig.PRINT_FILE, LogLevel.ERROR, msg);
    }

    /**
     * 分发消息，日志处理过程可同步执行也可以不执行，由{@link LogMsg#isSync}属性决定，在{@link Dispatcher}中处理。
     *
     * @param printMode 日志输出的类型，{@link LogConfig#PRINT_LOGCAT,LogConfig#PRINT_FILE}，前者只输出到Logcat，后者输出到文件。
     * @param logLevel  打印日志的类型，跟Android中的Log.d、Log.e一样~
     * @param msg       日志信息。
     */
    private void dispatcher(int printMode, int logLevel, String msg) {
        mDispatcher.dispatch(LogMsgPool.obtain(this, msg, mLogTag, logLevel, printMode));
    }

    /**
     * 开始处理消息，代码执行环境可以是子线程也可以是调用者线程。
     *
     * @param message 日志消息的载体。
     */
    public void dispatchChain(LogMsg message) {
        if (mRootChain == null) {
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

    /**
     * (注意:销毁日志框架，销毁之后日志框架不可用，重新使用要再次调用一下init()方法。)
     * 方法调用之后日志框架将不能正常工作，拦截器中的状态都被置为Close。
     */
    public void destroy() {
        mDispatcher.dispatch(LogMsgPool.obtainCloseMsg(this, "destroy"));
    }

    public static class Builder {
        private String logFilePath;
        private String logTag;
        private int logFileSize;
        private long deleteDay;
        private int threadModel;
        private int ioType;
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

        /**
         * 设置线程模式，供两个模式可选：线程池和单线程。其实这俩区别不大，主要是为了练手~
         *
         * @param threadModel {@link LogConfig#THREAD_POOL,LogConfig#THREAD_WORKER}
         */
        public Builder setThreadModel(int threadModel) {
            if (threadModel <= 0) {
                throw new IllegalArgumentException("Builder.setThreadModel() : threadModel must be 1 or 2 !");
            }
            this.threadModel = threadModel;
            return this;
        }

        LogCenter build() {
            if (TextUtils.isEmpty(logTag)) {
                logTag = TAG;
            }
            if (TextUtils.isEmpty(logFilePath)) {
                logFilePath = applicationContext.getExternalCacheDir().getAbsolutePath() + File.separator + "WeLog";
            }
            if (logFileSize <= 0) {
                logFileSize = LogConfig.M;
            }
            if (deleteDay <= 0) {
                deleteDay = LogConfig.DEFAULT_DELETE_DAY;
            }
            if (threadModel <= 0) {
                threadModel = LogConfig.THREAD_WORKER;
            }
            if (ioType <= 0) {
                ioType = LogConfig.NIO;
            }
            weLogInterceptors.add(new AndroidInterceptor());
            weLogInterceptors.add(new FormatLogMessageInterceptor());
            weLogInterceptors.add(new EncryptionInterceptor());
            weLogInterceptors.add(new FileConfigurationInterceptor());
            if(ioType == LogConfig.NIO) {
                weLogInterceptors.add(new NIOFileInterceptor());
            }else {
                weLogInterceptors.add(new IOFileInterceptor());
            }
            weLogInterceptors.add(new EndOfInterceptor());
            dispatcher = DispatcherFactory.create(threadModel);
            return new LogCenter(this);
        }

        private void checkParam(Object value, String msg) {
            if (null == value) {
                throw new IllegalArgumentException(msg);
            }
        }
    }
}
