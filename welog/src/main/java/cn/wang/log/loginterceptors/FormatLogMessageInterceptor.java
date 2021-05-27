package cn.wang.log.loginterceptors;

import android.annotation.SuppressLint;
import android.os.Process;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogMsg;

/**
 * Created to : 格式化日志，主要是为了按照格式输入到日志文件中。
 *
 * 2021-05-11 17:36:17 processId threadName:threadId cc.wang:
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class FormatLogMessageInterceptor implements WeLogInterceptor {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public LogMsg println(Chain chain) throws Exception{
        LogMsg target = chain.target();
        if ((target.printMode & LogConfig.CLOSE) != 0) {
            close();
            return chain.process(target);
        }
        StringBuilder sb = new StringBuilder();
        String format = dateFormat.format(new Date());
        sb.append(format)
                .append(" ")
                .append(Process.myPid())
                .append(" ")
                .append(Thread.currentThread().getName())
                .append("/")
                .append(Thread.currentThread().getId())
                .append(" ")
                .append(target.tag)
                .append(":")
                .append(target.message);
        target.resetMessage(sb.toString());
        return chain.process(target);
    }

    @Override
    public void close() {

    }
}
