package cn.wang.welog.fps.core;

import android.util.Log;
import android.view.Choreographer;
import java.lang.reflect.Method;
import cn.wang.welog.fps.utils.ReflectUtils;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/10
 */
class ReflectChoreographer {

    Object[] addTraversalQueue;
    Object displayEventReceiver;
    Object callbackQueueLock;
    Method addTraversalCallback;
    Choreographer choreographer;
    long frameIntervalNanos;

    void init() {
        choreographer = Choreographer.getInstance();
        addTraversalQueue = ReflectUtils.reflectObject(choreographer,FpsConstants.CALLBACK_QUEUE,null);
        callbackQueueLock = ReflectUtils.reflectObject(choreographer, "mLock", new Object());
        if(null != addTraversalQueue){
            addTraversalCallback = ReflectUtils.reflectMethod(addTraversalQueue[FpsConstants.CALLBACK_TRAVERSAL],FpsConstants.ADD_CALLBACK_LOCKED,long.class, Object.class, Object.class);
        }
        displayEventReceiver = ReflectUtils.reflectObject(choreographer, FpsConstants.DISPLAY_EVENT_RECEIVER, null);
        frameIntervalNanos = ReflectUtils.reflectObject(choreographer, FpsConstants.FRAME_INTERVAL_NANOS, FpsConstants.DEFAULT_FRAME_DURATION);
    }


    long getIntendedFrameTimeNs(long defaultValue) {
        try {
            return ReflectUtils.reflectObject(displayEventReceiver, "mTimestampNanos", defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("WANG","ReflectChoreographer.getIntendedFrameTimeNs");
        return defaultValue;
    }



}
