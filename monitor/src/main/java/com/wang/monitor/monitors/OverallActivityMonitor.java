package com.wang.monitor.monitors;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.FrameMetrics;
import android.view.Window;

import androidx.annotation.NonNull;

import com.wang.monitor.core.AppMonitor;
import com.wang.monitor.interfaces.ActivityLifeCircleObserver;
import com.wang.monitor.interfaces.Monitor;

import java.util.HashMap;
import java.util.Map;

import cn.wang.log.core.WeLog;


/**
 * Created to : 统计界面的启动时间。
 *
 * @author cc.wang
 * @date 2021/6/17
 */
public class OverallActivityMonitor implements ActivityLifeCircleObserver, Monitor {

    public static void staticWindowFocusChanged(String name) {
        OverallActivityMonitor overallActivityMonitor = AppMonitor.getInstance().getActivityStartupMonitor();
        if (null != overallActivityMonitor) {
            overallActivityMonitor.onWindowFocusChanged(name);
        }
    }

    private Map<String, Long> timeCollection = new HashMap<>();
    private SparseArray<String> activityLifeCircle = new SparseArray();

    private static Window.OnFrameMetricsAvailableListener frameMetricsAvailableListener;

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        activityLifeCircle.put(activity.hashCode(), activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity) {
        timeCollection.put(activity.getClass().getSimpleName(), System.currentTimeMillis());
        //addFrameMetricObserver(activity.getWindow());
    }

    @Override
    public void onActivityPause(@NonNull Activity activity) {
        activityLifeCircle.remove(activity.hashCode());
    }

    /**
     * 因为监听是设置到ThreadRenderer上的，所以一般一个应用只会创建一个实例对象。所以监听只需要设置一次，设置之后就可以监听应用的绘制。
     * 但要注意的是，监听的话要区分Dialog 跟 Activity,且该API只能在Android 7.0以上才能使用。
     *
     * @param window
     */
    private void addFrameMetricObserver(Window window) {
        if (!IoThread.getIoThread().isAlive()) {
            return;
        }
        WeLog.d("ActivityMonitor.addFrameMetricObserver start !");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (null == frameMetricsAvailableListener) {
                frameMetricsAvailableListener = new Window.OnFrameMetricsAvailableListener() {
                    @Override
                    public void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int dropCountSinceLastInvocation) {
                        Context context = window.getContext();
                        if (null == activityLifeCircle.get(context.hashCode())) {
                            return;
                        }
                        FrameMetrics frameMetricsCopy = new FrameMetrics(frameMetrics);
                        long metric = frameMetricsCopy.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION);
                        long metricDraw = frameMetricsCopy.getMetric(FrameMetrics.DRAW_DURATION);
                        long metricSync = frameMetricsCopy.getMetric(FrameMetrics.SYNC_DURATION);
                        if (metric / 10000000L != 0) {
                            Log.e("FrameMetrics", context.getClass().getName() + ",LAYOUT_MEASURE_DURATION -> " + metric / 10000000);
                        }
                        if (metricDraw / 10000000L != 0) {
                            Log.e("FrameMetrics", context.getClass().getName() + ",DRAW_DURATION -> " + metricDraw / 10000000);
                        }

                        if (metricSync / 10000000L != 0) {
                            Log.e("FrameMetrics", context.getClass().getName() + ",SYNC_DURATION -> " + metricSync / 10000000);
                        }
                    }
                };
            }
            window.addOnFrameMetricsAvailableListener(frameMetricsAvailableListener, IoThread.getIoThread().getIoHandler());
        }
    }

    public void onWindowFocusChanged(String activityName) {
        Long l = timeCollection.get(activityName);
        if (l != null && l > 0L) {
            long end = System.currentTimeMillis() - l;
            WeLog.d("ActivityMonitor: " + activityName + " start , time =  " + end + "ms");
        }
        timeCollection.remove(activityName);
    }

    @Override
    public void monitorStart() {

    }

    @Override
    public void monitorPause() {

    }

    @Override
    public void monitorResume() {

    }

    @Override
    public void monitorQuit() {
        timeCollection.clear();
    }
}
