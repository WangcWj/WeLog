package com.wang.monitor.core;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.wang.monitor.interfaces.ActivityLifeCircleObserver;
import com.wang.monitor.interfaces.Monitor;
import com.wang.monitor.monitors.OverallActivityMonitor;
import com.wang.monitor.monitors.FpsFramesMonitor;
import com.wang.monitor.monitors.LooperMonitor;
import com.wang.monitor.monitors.MainThreadBlockMonitor;

import cn.wang.log.core.WeLog;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/10
 */
public class AppMonitor {

    private AppMonitor() {
    }

    private static class Instance {
        private static AppMonitor appMonitor = new AppMonitor();
    }

    public static AppMonitor getInstance() {
        return Instance.appMonitor;
    }

    private ReflectChoreographer mReChoreographer;
    private volatile boolean mHaveInit = false;
    private boolean mIsRunning = false;
    private int mActivityCount = 0;
    private int mResumeActivityCount = 0;
    private int mRunState = MonitorConstants.UN_INIT;
    private ActivityLifeCircle mActivityLifeCircle;
    private final Map<String, ActivityLifeCircleObserver> mActivityObservers = new HashMap<>(10);
    private final Map<String, Monitor> mMonitors = new HashMap<>(10);

    /**
     * 1.初始化Choreographer。
     * 2.
     *
     * @param application
     */
    public void init(Application application) {
        if (mHaveInit) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return;
        }
        mReChoreographer = new ReflectChoreographer();
        mReChoreographer.init();
        registerActivityLifeCircle(application);
        initMonitors();
        mHaveInit = true;
    }

    /**
     * 添加对应用中Activity的生命周期的监控。
     *
     * @param application
     */
    private void registerActivityLifeCircle(Application application) {
        if (null == mActivityLifeCircle) {
            mActivityLifeCircle = new ActivityLifeCircle();
        }
        application.unregisterActivityLifecycleCallbacks(mActivityLifeCircle);
        application.registerActivityLifecycleCallbacks(mActivityLifeCircle);
    }

    /**
     * 1.初始化Looper监听。
     * 2.初始化FPS监视器。
     * 3.初始化主线程阻塞监控的监视器。
     * 4.初始化Activity启动时间监控的监视器。
     */
    private void initMonitors() {
        LooperMonitor looperMonitor = new LooperMonitor();
        FpsFramesMonitor fpsFramesMonitor = new FpsFramesMonitor(mReChoreographer);
        MainThreadBlockMonitor mainThreadBlockMonitor = new MainThreadBlockMonitor(1000, 50);
        OverallActivityMonitor overallActivityMonitor = new OverallActivityMonitor();
        addActivityObserver(overallActivityMonitor);
        looperMonitor.addLoopListener(mainThreadBlockMonitor);
        looperMonitor.addLoopListener(fpsFramesMonitor);
        //监控生命周期。
        mMonitors.put(looperMonitor.getClass().getSimpleName(), looperMonitor);
        //mMonitors.put(mainThreadBlockMonitor.getClass().getSimpleName(), mainThreadBlockMonitor);
        mMonitors.put(fpsFramesMonitor.getClass().getSimpleName(), fpsFramesMonitor);
        //mMonitors.put(overallActivityMonitor.getClass().getSimpleName(), overallActivityMonitor);
    }

    /**
     * 所有注册的Monitor启动监听。
     */
    public void onStart() {
        if (!mHaveInit || mIsRunning) {
            return;
        }
        WeLog.d("AppMonitor start !");
        if (mMonitors.size() > 0) {
            for (Monitor m : mMonitors.values()) {
                m.monitorStart();
            }
        }
        mIsRunning = true;
    }

    /**
     * 添加Activity生命周期监控的回调，Monitor组件会用到。
     *
     * @param observer
     */
    public void addActivityObserver(ActivityLifeCircleObserver observer) {
        if (!mActivityObservers.containsKey(observer.getClass().getSimpleName())) {
            mActivityObservers.put(observer.getClass().getSimpleName(), observer);
        }
    }

    /**
     * 移除回调。
     *
     * @param observer
     */
    public void removeActivityObserver(ActivityLifeCircleObserver observer) {
        mActivityObservers.remove(observer.getClass().getSimpleName());
    }

    /**
     * 获取Activity启动时间监控的Monitor。
     *
     * @return
     */
    public OverallActivityMonitor getActivityStartupMonitor() {
        return (OverallActivityMonitor) mMonitors.get(OverallActivityMonitor.class.getSimpleName());
    }

    public boolean isForeground() {
        return mRunState == MonitorConstants.RESUME;
    }

    private void applicationExit() {

    }

    private void applicationIsBackground(boolean background) {
        mRunState = background ? MonitorConstants.PAUSE : MonitorConstants.RESUME;
    }

    /**
     * 监听应用中Activity的状态。
     */
    private class ActivityLifeCircle implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            mActivityCount++;
            if (mActivityObservers.size() > 0) {
                for (ActivityLifeCircleObserver observer : mActivityObservers.values()) {
                    observer.onActivityCreated(activity);
                }
            }
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            mResumeActivityCount++;
            applicationIsBackground(false);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            if (mActivityObservers.size() > 0) {
                for (ActivityLifeCircleObserver observer : mActivityObservers.values()) {
                    observer.onActivityResumed(activity);
                }
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            if (mActivityObservers.size() > 0) {
                for (ActivityLifeCircleObserver observer : mActivityObservers.values()) {
                    observer.onActivityPause(activity);
                }
            }
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            mResumeActivityCount--;
            if (mResumeActivityCount <= 0) {
                mResumeActivityCount = 0;
                applicationIsBackground(true);
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            mActivityCount--;
            if (mActivityCount <= 0) {
                applicationExit();
            }
        }
    }

    /**
     * 腾讯的Matrix使用的是该方法来判断应用是否在前台，没有使用int记录，可能是因为应用中有检测不到的Activity吧。
     * @return
     */
    public static String getTopActivityName() {
        long start = System.currentTimeMillis();
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map<Object, Object> activities;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                activities = (HashMap<Object, Object>) activitiesField.get(activityThread);
            } else {
                activities = (ArrayMap<Object, Object>) activitiesField.get(activityThread);
            }
            if (activities.size() < 1) {
                return null;
            }
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity.getClass().getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

}
