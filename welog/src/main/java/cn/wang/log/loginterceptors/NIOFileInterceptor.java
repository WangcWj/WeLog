package cn.wang.log.loginterceptors;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.nio.MappedByteBuffer;

import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogCenter;
import cn.wang.log.core.LogMsg;
import cn.wang.log.exceptions.OpenFileException;
import cn.wang.log.utils.WeLogFileUtils;
import cn.wang.niolib.NioUtils;

/**
 * Created to : 如果当前的日志需要输入到文件中，则写入文件。
 * 1.目前采用的是标注的IO流，性能上存在瓶颈，不如使用内存映射。
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class NIOFileInterceptor implements WeLogInterceptor {

    private boolean mIsFinish;
    private MappedByteBuffer bufferedWriter;

    public NIOFileInterceptor() {
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
        if (target.printMode >= 0 && (target.printMode & LogConfig.PRINT_FILE) == 0) {
            return chain.process(target);
        }
        openFileMap(target);
        //写入缓存
        bufferedWriter.put(target.message.getBytes());
        return chain.process(target);
    }


    /**
     * c++中的mmap()函数是吧一个文件或者是一块内存区对象映射到调用方法的用户进程的地址空间中。
     * 1.Linus中的物理内存是分页管理的，一个分页的大小都是2的次幂，通常使用 2k 4k 8k等来定义一个分页的大小，
     * 当使用mmap()函数将一个文件映射到物理内存中时，如果文件映射的大小跟物理内存的大小不一致时，会产生一点微妙的变化。因为文件
     * 映射到物理内存中的大小要符合物理内存分页管理的规范，如果映射大小不足一页时，物理内存实际上也为该文件分配了一页的大小，所以通常
     * 在使用mmap()函数映射一个文件之前，会使用ftruncate()函数，来让
     * 这时，在进程中对内存映射地址操作时就会出现一些问题，下面分情况分析：
     * 假设物理内存分页大小为2k 也就是4096个字节。
     * 1.
     * 比如：文件映射大小为 5000个字节，超过了一个物理分页，所以操作系统会为之分配两个内存分页，也就是8192个字节。
     * 当应用程序访问0-4999个字节空间时，可以正常的读写，内存也可以写入物理内存中，但访问超过4999-8192个字节空间时，
     * 此时访问的指针已经超过了映射区，但未超过物理区，此时操作系统是允许对超过的物理内存进行读写的，但虽支持读写实际上完全写不进去内容，
     * 如果应用程序访问超过分配的物理内存时，就会发出SIGSEGV信号。
     * <p>
     * 总结就是：
     * （1）没超过物理页面，没超过映射区大小 —> 正常读写
     * （2）没超过物理页面，超过映射区大小 —> 内核允许读写但不执行写入操作
     * （3）超过物理页面，没有超过映射区大小 —> 引发SIGBUS信号
     * （4）超过物理页面， 超过映射区大小 —> 引发SIGSEGV信号
     *
     *
     * <p>
     * 遇到的问题：
     * 1.日志文件追加写入。
     * 当进程重新启动的时候 怎么追加内容到原来的日志文件上。进程启动之后，需要先建立与日志文件的映射关系，建立之后，如果在写
     * 数据的时候不进行追加写入，就会导致新内容覆盖掉原文件的内容。
     * 2.MappedByteBuffer资源释放，并没有显示的API，采用反射调用会有风险。
     *
     * </P>
     *
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void openFileMap(LogMsg msg) {
        if (null == bufferedWriter) {
            bufferedWriter = NioUtils.create(msg.logFile.getAbsolutePath(), msg.log.getLogFileSize());
            if (null == bufferedWriter) {
                throw new OpenFileException("Nio: Failed to open mapped file");
            }
            //追加写入文件。
            int index = 0;
            while (bufferedWriter.get(index) != 0) {
                index++;
            }
            //当文件内容的大小跟限制文件的大小之间相差5k的话，就重新创建新文件。
            if (checkFileSize(msg, index)) {
                createNewFile(msg);
                return;
            }
            bufferedWriter.position(index);
        } else {
            if (msg.logFileIsChange) {
                //重新映射。
                closeBuffer();
                msg.logFileIsChange = false;
                openFileMap(msg);
            } else if (checkFileSize(msg, bufferedWriter.position())) {
                createNewFile(msg);
            }
        }
    }

    private void createNewFile(LogMsg msg) {
        msg.logFileChange(true);
        String newName = msg.logFile.getName();
        int fileSuffix = WeLogFileUtils.getFileSuffix(newName);
        if (newName.contains(WeLogFileUtils.FUFFIX)) {
            newName = newName.substring(0, newName.indexOf(WeLogFileUtils.FUFFIX));
        }
        msg.resetLogFile(WeLogFileUtils.createCopyFile(msg.logFile, TextUtils.concat(newName + WeLogFileUtils.FUFFIX + (fileSuffix + 1)).toString()));
        openFileMap(msg);
    }

    private boolean checkFileSize(LogMsg msg, int position) {
        Log.e("cc.wang", "IOFileInterceptor.openFileMap.index  " + position+" size   is   "+(msg.log.getLogFileSize() - LogConfig.K_5));
        return position >= msg.log.getLogFileSize() - LogConfig.K_5;
    }

    @Override
    public void close() {
        mIsFinish = true;
        closeBuffer();
    }

    private void closeBuffer() {
        if (bufferedWriter != null) {
            NioUtils.clean(bufferedWriter);
        }
        bufferedWriter = null;
    }
}
