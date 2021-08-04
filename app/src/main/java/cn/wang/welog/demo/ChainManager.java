package cn.wang.welog.demo;

import java.util.List;

/**
 * Created to :
 *
 * @author Wang
 * @date 2021/6/22
 */
public class ChainManager implements Handlers.Chain {


    private List<Handlers> handlers;
    private int index;
    private Object request;

    public ChainManager(List<Handlers> handlers, int index) {
        this.handlers = handlers;
        this.index = index;
    }

    @Override
    public Object progress(Object o) {
        if (index >= handlers.size()) {
            throw new IllegalStateException("");
        }
        this.request = o;
        ChainManager chainManager = new ChainManager(this.handlers, index + 1);
        Handlers handlers = this.handlers.get(index);
        return handlers.Handler(chainManager);
    }

    @Override
    public Object request() {
        return request;
    }
}
