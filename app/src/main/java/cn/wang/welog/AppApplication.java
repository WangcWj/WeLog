package cn.wang.welog;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.wang.log.config.LogConfig;
import cn.wang.log.core.LogCenter;
import cn.wang.log.core.WeLog;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/12
 */
public class AppApplication extends Application {

    private int mActivityCount = 0;

    public static int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        LogCenter.Builder builder = new LogCenter.Builder(this);
        WeLog.init(builder);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                mActivityCount++;
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                mActivityCount--;
                if (mActivityCount <= 0) {
                    //
                }
            }
        });
    }
}
