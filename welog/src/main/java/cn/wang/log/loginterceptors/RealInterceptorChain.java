package cn.wang.log.loginterceptors;

import androidx.annotation.NonNull;

import java.util.List;

import cn.wang.log.core.LogMsg;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/10
 */
public class RealInterceptorChain implements WeLogInterceptor.Chain {

    private List<WeLogInterceptor> weLogInterceptors;
    private int index;

    private LogMsg target;

    public RealInterceptorChain(List<WeLogInterceptor> weLogInterceptors, int index) {
        this.weLogInterceptors = weLogInterceptors;
        this.index = index;
    }

    public RealInterceptorChain(List<WeLogInterceptor> weLogInterceptors, int index, @NonNull LogMsg msg) {
        this.weLogInterceptors = weLogInterceptors;
        this.index = index;
        this.target = msg;
    }

    @Override
    public LogMsg process(LogMsg msg) throws Exception {
        target = msg;
        if (index >= weLogInterceptors.size()) {
            throw new RuntimeException("index out of bounds ! index is " + index + "  size  is " + weLogInterceptors.size());
        }
        WeLogInterceptor weLogInterceptor = weLogInterceptors.get(index);
        RealInterceptorChain next = new RealInterceptorChain(weLogInterceptors, index + 1, target);
        return weLogInterceptor.println(next);
    }

    @Override
    public LogMsg target() {
        return target;
    }
}
