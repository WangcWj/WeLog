package cn.wang.log.dispatchers;

import cn.wang.log.config.LogConfig;

/**
 * Created to : 创建异步环境执行类。
 *
 * @author cc.wang
 * @date 2021/5/26
 */
public class DispatcherFactory {

    public static Dispatcher create(int model) {
        if (model == LogConfig.THREAD_WORKER) {
            return new ThreadWorkerDispatcher();
        } else {
            return new ThreadPoolDispatcher();
        }
    }

}
