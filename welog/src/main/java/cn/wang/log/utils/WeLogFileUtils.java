package cn.wang.log.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/12
 */
public class WeLogFileUtils {

    public static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static long getTimeForDay(long time) {
        long tempTime = 0;
        try {
            String dataStr = sDateFormat.format(new Date(time));
            tempTime = sDateFormat.parse(dataStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempTime;
    }


    public static File createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }
        if (!file.exists() && null != file.getParent()) {
            File parent = new File(file.getParent());
            if (!parent.exists()) {
                if (parent.mkdirs()) {
                    return file;
                }
            } else {
                return file;
            }
        }
        return null;
    }

    public static boolean createDirectory(File file, String filePath) {
        file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return !file.mkdirs();
    }

    public static File getLastModifyFile(File file) {
        long modify = 0;
        File chileFile = null;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File f : files) {
                    long l = f.lastModified();
                    if (l > modify) {
                        modify = l;
                        chileFile = f;
                    }
                }
            }
        }
        return chileFile;
    }

    public static void checkCanDeleteFile(File directory, long saveTime) {
        long currentTime = getTimeForDay(System.currentTimeMillis());
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File f : files) {
                    long timeForDay = getTimeForDay(f.lastModified());
                    if (currentTime - timeForDay > saveTime) {
                        f.delete();
                    }
                }
            }
        }
    }

    private File[] cleanLogFilesIfNecessary(String folderPath) {
        File logDir = new File(folderPath);
        return logDir.listFiles();
    }

    public static File createCopyFile(File file, String name) {
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                return new File(parentFile.getAbsolutePath() + File.separator + name);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean fileIsExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

}
