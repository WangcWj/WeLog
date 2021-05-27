package cn.wang.log.utils;

import java.util.concurrent.ThreadFactory;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/25
 */
public class WeLogThreadUtils {

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

}
