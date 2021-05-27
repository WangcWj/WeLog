package cn.wang.log.loginterceptors;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogMsg;
import cn.wang.log.core.LogCenter;
import cn.wang.log.exceptions.LogFileException;
import cn.wang.log.exceptions.OpenFileException;
import cn.wang.niolib.NioUtils;

/**
 * Created to : 如果当前的日志需要输入到文件中，则写入文件。
 * 1.目前采用的是标注的IO流，性能上存在瓶颈，不如使用内存映射。
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class IOFileInterceptor implements WeLogInterceptor {

    private LogCenter log;
    private boolean mIsFinish;
    private BufferedWriter bufferedWriter;

    public IOFileInterceptor() {
        mIsFinish = false;
    }

    @Override
    public LogMsg println(Chain chain) throws Exception {
        LogMsg target = chain.target();
        if (mIsFinish) {
            return chain.process(target);
        }
        if ((target.printMode & LogConfig.CLOSE) != 0) {
            close();
            return chain.process(target);
        }
        log = target.log;
        if (target.printMode >= 0 && (target.printMode & LogConfig.PRINT_FILE) == 0) {
            return chain.process(target);
        }
        createFileBuffer(target);
        //写入缓存
        bufferedWriter.write(target.message);
        bufferedWriter.flush();
        return chain.process(target);
    }

    @Override
    public void close() {
        mIsFinish = true;
        closeBuffer();
    }

    private void closeBuffer() {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bufferedWriter = null;
    }

    /**
     * 使用一般的IO流形式来操作日志文件。
     *
     * @throws IOException
     */
    private void createFileBuffer(LogMsg msg) throws IOException {
        if (null == bufferedWriter) {
            if (TextUtils.isEmpty(log.getLogFilePath()) || log.getLogFileSize() <= 0) {
                throw new IllegalArgumentException("Please check that the file parameters are incorrect! file path "
                        + log.getLogFilePath() + "  file size  " + log.getLogFileSize());
            }
            if (null == msg.logFile) {
                throw new LogFileException("What ? The logfile is null !");
            }
            bufferedWriter = new BufferedWriter(new FileWriter(msg.logFile, true));
        } else if (msg.logFileIsChange) {
            closeBuffer();
            msg.logFileChange(false);
            createFileBuffer(msg);
        }
    }
}
