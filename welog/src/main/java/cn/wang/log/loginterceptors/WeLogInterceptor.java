package cn.wang.log.loginterceptors;

import cn.wang.log.core.LogMsg;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public interface WeLogInterceptor {

    LogMsg println(Chain chain) throws Exception;

    void close() ;

    interface Chain{

        LogMsg process(LogMsg msg) throws Exception;

        LogMsg target();

    }

}
