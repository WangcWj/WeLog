package cn.wang.niolib;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * @author cc.wang
 * @date
 */
public class NioUtils {


    public static void clean(final Object buffer) {
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
                    getCleanerMethod.setAccessible(true);
                    sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
                    cleaner.clean();
                }catch (Exception e){
                   e.printStackTrace();
                }
                return null;
            }
        });
    }

    public static MappedByteBuffer create(String path, int mapSize) {
        MappedByteBuffer byteBuffer = null;
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(path, "rw");
            byteBuffer = rf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, mapSize);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (rf != null) {
                try {
                    rf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return byteBuffer;
    }

}