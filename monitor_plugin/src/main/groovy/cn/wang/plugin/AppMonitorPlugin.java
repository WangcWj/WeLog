package cn.wang.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/6/17
 */
public class AppMonitorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().getByType(AppExtension.class).registerTransform(new AppMonitorTransform());
    }
}
