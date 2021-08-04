package com.wang.monitor.core;

import android.view.Choreographer;
import java.lang.reflect.Method;
import com.wang.monitor.utils.ReflectUtils;

/**
 * Created to : 用来反射获取Choreographer中的方法，用于发起绘制请求。
 *
 * @author WANG
 * @date 2021/6/10
 */
public class ReflectChoreographer {

    Object[] addTraversalQueue;
    Object displayEventReceiver;
    Object callbackQueueLock;
    Method addTraversalCallback;
    Choreographer choreographer;
    public long frameIntervalNanos;

    void init() {
        choreographer = Choreographer.getInstance();
        addTraversalQueue = ReflectUtils.reflectObject(choreographer, MonitorConstants.CALLBACK_QUEUE,null);
        callbackQueueLock = ReflectUtils.reflectObject(choreographer, "mLock", new Object());
        if(null != addTraversalQueue){
            addTraversalCallback = ReflectUtils.reflectMethod(addTraversalQueue[MonitorConstants.CALLBACK_TRAVERSAL], MonitorConstants.ADD_CALLBACK_LOCKED,long.class, Object.class, Object.class);
        }
        displayEventReceiver = ReflectUtils.reflectObject(choreographer, MonitorConstants.DISPLAY_EVENT_RECEIVER, null);
        frameIntervalNanos = ReflectUtils.reflectObject(choreographer, MonitorConstants.FRAME_INTERVAL_NANOS, MonitorConstants.DEFAULT_FRAME_DURATION);
    }

    public Object getCallbackQueueLock() {
        return callbackQueueLock;
    }

    public Method getAddTraversalCallback() {
        return addTraversalCallback;
    }

    public Object[] getAddTraversalQueue() {
        return addTraversalQueue;
    }

    public long getIntendedFrameTimeNs(long defaultValue) {
        try {
            return ReflectUtils.reflectObject(displayEventReceiver, "mTimestampNanos", defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }



}
