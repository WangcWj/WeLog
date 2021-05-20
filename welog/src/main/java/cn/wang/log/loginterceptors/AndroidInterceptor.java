package cn.wang.log.loginterceptors;

import android.util.Log;
import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogMsg;


/**
 * Created to : 用来输出日志到Logcat。
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class AndroidInterceptor implements WeLogInterceptor {

    @Override
    public LogMsg println(Chain chain) {
        LogMsg target = chain.target();
        LogMsg process = null;
        //捕获后续链路中的异常信息，将异常信息打印出来，如有特殊需求，可以根据抛出异常的类型来详细区分。
        try {
            if (target.printMode >= 0 && (target.printMode & LogConfig.PRINT_LOGCAT) != 0) {
                Log.println(target.level, target.tag, target.message);
            }
            if ((target.printMode ^ LogConfig.PRINT_LOGCAT) != 0) {
                process = chain.process(target);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Log.e(target.tag, "AndroidInterceptor.Exception: " + message);
        }
        return process;
    }

    @Override
    public void close(Chain chain) {

    }
}
