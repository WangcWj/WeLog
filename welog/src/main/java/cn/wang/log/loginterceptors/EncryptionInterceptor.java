package cn.wang.log.loginterceptors;

import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogMsg;

/**
 * Created to : 加密日志文件。
 *
 * 2020:12:12
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class EncryptionInterceptor implements WeLogInterceptor {

    @Override
    public LogMsg println(Chain chain) throws Exception{
        LogMsg target = chain.target();
        if ((target.printMode & LogConfig.CLOSE) != 0) {
            return chain.process(target);
        }
        target.resetMessage(target.message+"\n");
        return chain.process(target);
    }

    @Override
    public void close() {

    }
}
