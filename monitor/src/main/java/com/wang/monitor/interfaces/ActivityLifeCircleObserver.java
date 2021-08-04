package com.wang.monitor.interfaces;

import android.app.Activity;

import androidx.annotation.NonNull;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/6/17
 */
public interface ActivityLifeCircleObserver {

    void onActivityResumed(@NonNull Activity activity);

    void onActivityCreated(@NonNull Activity activity);

    void onActivityPause(@NonNull Activity activity);

}
