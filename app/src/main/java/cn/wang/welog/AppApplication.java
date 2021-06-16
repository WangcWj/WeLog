package cn.wang.welog;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.wang.log.core.LogCenter;
import cn.wang.log.core.WeLog;

import com.github.moduth.blockcanary.BlockCanary;
import com.wang.monitor.fps.core.AppMonitor;

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
        builder.setLogTag("WangMonitor");
        WeLog.init(builder);
        AppMonitor.getInstance().init();
    }
}
