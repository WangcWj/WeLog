package com.wang.monitor.fps.interfaces;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/14
 */
public interface FpsFrameCallback {

    void doFrame(long endTime, long intendedFrameTimeNs, long frameIntervalNanos);
}
