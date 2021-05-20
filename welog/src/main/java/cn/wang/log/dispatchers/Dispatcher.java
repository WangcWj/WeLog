package cn.wang.log.dispatchers;

import cn.wang.log.core.LogMsg;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/13
 */
public interface Dispatcher {

    void dispatch(LogMsg message);

    void shutDown();
}
