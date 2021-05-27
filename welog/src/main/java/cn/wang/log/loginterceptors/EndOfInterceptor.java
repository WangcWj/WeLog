package cn.wang.log.loginterceptors;

import cn.wang.log.core.LogMsg;

/**
 * Created to : 日志处理链表的最后一个环节，用来结束请求。
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class EndOfInterceptor implements WeLogInterceptor {
    @Override
    public LogMsg println(Chain chain) {
        return chain.target();
    }

    @Override
    public void close() {

    }
}
