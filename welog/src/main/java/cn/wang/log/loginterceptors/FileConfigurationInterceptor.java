package cn.wang.log.loginterceptors;

import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogMsg;
import cn.wang.log.utils.WeLogFileUtils;

/**
 * Created to : 在写日志前对日志文件的属性做一些判断。
 * 1。日志文件的名称
 * 2.日志文件是否需要删除。
 * 3.日志文件大小超过设定大小时，下次是否需要创建新的文件。
 *
 * <p>
 * targetLogFile为null：表示框架第一次执行。这时需要做两个条件的判断：
 * 1.有无日志文件，没有的话创建新日志文件。
 * 2.根据旧文件判断是否需要新建新的日志文件，可能从文件的大小跟文件的日期来确定。
 * targetLogFile不为null：
 * 表示当前应用一直在执行，其实也要判断下该文件的大小跟日期。
 * <p>
 * <p/>
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class FileConfigurationInterceptor implements WeLogInterceptor {

    private File targetLogFile;
    private boolean mIsFinish;

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
        //检查SDCard可用内存空间。
        //第一次执行，可能为null，此时有两种情况。
        if (targetLogFile == null) {
            File logFile = new File(target.log.getLogFilePath());
            if (logFile.exists()) {
                //日志文件夹存在，找出最近修改的日志文件。
                File lastModifyFile = WeLogFileUtils.getLastModifyFile(logFile);
                if (null != lastModifyFile) {
                    targetLogFile = lastModifyFile;
                }
                //只在应用进程启动的时候检查一次日志是否需要删除
                WeLogFileUtils.checkCanDeleteFile(logFile, target.log.getDeleteDay());
            }
        }
        //没有找到历史日志文件。
        if (targetLogFile == null) {
            targetLogFile = WeLogFileUtils.createFile(target.log.getLogFilePath() + File.separator + generaFileName());
        }
        if (targetLogFile == null) {
            throw new FileNotFoundException("FileConfigurationInterceptor: File creation failed,new targetLogFile is null !");
        }
        //查看日志的日期，默认是一天一个日志文件。
        long currentTimeMillis = System.currentTimeMillis();
        if (needCreateNewFile(targetLogFile, currentTimeMillis)) {
            targetLogFile = WeLogFileUtils.createCopyFile(targetLogFile, WeLogFileUtils.sDateFormat.format(currentTimeMillis));
            target.logFileChange(true);
        }
        if (targetLogFile == null) {
            throw new FileNotFoundException("FileConfigurationInterceptor: File creation failed,new targetLogFile is null !");
        }
        //对NIO方式写文件的话，这边就要稍微的处理一下逻辑了。
        //这里其实日志文件有可能超过固定的大小，可能上一次写入的数据大小刚好比logFileSize大一点。
        //NIO 由于是采用的内存映射文件，所以不能提前获取文件的大小。
        if (target.log.getIoType() == LogConfig.IO) {
            if (targetLogFile.length() >= target.log.getLogFileSize()) {
                int fileSuffix = WeLogFileUtils.getFileSuffix(targetLogFile.getName());
                String newName = targetLogFile.getName();
                if (targetLogFile.getName().contains(WeLogFileUtils.FUFFIX)) {
                    newName = newName.substring(0, targetLogFile.getName().indexOf(WeLogFileUtils.FUFFIX));
                }
                targetLogFile = WeLogFileUtils.createCopyFile(targetLogFile, TextUtils.concat(newName + WeLogFileUtils.FUFFIX + (fileSuffix + 1)).toString());
                target.logFileChange(true);
            }
        }
        //必须重置日志文件，要不然下一步就没办法写日志文件。
        target.resetLogFile(targetLogFile);
        return chain.process(target);
    }

    @Override
    public void close() {
        Log.e("cc.wang", "FileConfigurationInterceptor.close.");
        mIsFinish = true;
        targetLogFile = null;
    }

    /**
     * SD卡是否可以写入文件。
     * <p>
     * if(!isCanWriteSDCard(target.log.getLogFilePath())){
     * throw new SdCardNotAvailableException("The size of the sdcard is less than 50m !");
     * <p>
     * </>
     *
     * @return
     */
    private boolean isCanWriteSDCard(String path) {
        boolean item = false;
        try {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            long total = availableBlocks * blockSize;
            if (total >= LogConfig.MIN_SDCARD_SIZE) {
                item = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 目的是一天写一个日志文件。
     * 判断文件的最后修改时间是不是“今天”，不是的话就重新创建一个日志文件。
     *
     * @param targetLogFile
     * @param currentTimeMillis
     * @return
     */
    private boolean needCreateNewFile(File targetLogFile, long currentTimeMillis) {
        long lastModified = targetLogFile.lastModified();
        if (lastModified <= 0) {
            return false;
        }
        if (!WeLogFileUtils.sDateFormat.format(currentTimeMillis).equals(WeLogFileUtils.sDateFormat.format(lastModified)) && currentTimeMillis > lastModified) {
            return true;
        }
        return false;
    }

    /**
     * 生成日志文件名称。
     *
     * @return
     */
    private String generaFileName() {
        return WeLogFileUtils.sDateFormat.format(new Date());
    }

}
