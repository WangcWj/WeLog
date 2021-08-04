package cn.wang.welog.demo;

/**
 * Created to :
 *
 * @author Wang
 * @date 2021/6/22
 */
public interface Handlers {

    Object Handler(Chain chain);

    interface Chain{

        Object progress(Object o);

        Object request();
    }
}
