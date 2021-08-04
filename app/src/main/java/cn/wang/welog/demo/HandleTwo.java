package cn.wang.welog.demo;

/**
 * Created to :
 *
 * @author Wang
 * @date 2021/6/22
 */
public class HandleTwo implements Handlers {

    @Override
    public Object Handler(Handlers.Chain chain) {
        return chain.progress(chain.request());
    }
}
