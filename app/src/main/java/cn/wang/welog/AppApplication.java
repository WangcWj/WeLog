package cn.wang.welog;


import android.app.Application;

import com.wang.monitor.core.AppMonitor;

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
        builder.setLogTag("AppMonitor");
        WeLog.init(builder);
        AppMonitor instance = AppMonitor.getInstance();
        instance.init(this);
        instance.onStart();
    }
}
