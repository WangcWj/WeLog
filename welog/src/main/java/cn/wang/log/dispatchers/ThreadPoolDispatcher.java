package cn.wang.log.dispatchers;

import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogMsg;
import cn.wang.log.utils.WeLogThreadUtils;

/**
 * Created to : 采用线程池+LinkedBlockingDeque的形式来处理任务。
 *
 * @author cc.wang
 * @date 2021/5/11
 */
public class ThreadPoolDispatcher implements Dispatcher {
    private ExecutorService executorService;

    @Override
    public void dispatch(LogMsg message) {
        Log.e("cc.wang","ThreadPoolDispatcher.dispatch."+message.hashCode());
        if (message.isSync && !isForcedAsync(message)) {
            message.run();
        } else {
            executorService().execute(message);
        }
    }

    @Override
    public void shutDown(){
        if(null != executorService && !executorService.isShutdown()){
            executorService.shutdownNow();
        }
        executorService = null;
    }

    private boolean isForcedAsync(LogMsg msg){
        return msg.printMode == LogConfig.PRINT_FILE;
    }

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<Runnable>(), WeLogThreadUtils.threadFactory("WeLog Thread:", false));
        }
        return executorService;
    }


}
