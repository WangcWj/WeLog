package cn.wang.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created to :
 *
 * @author cc.wang*
 * @date 2021/6/17
 */
class AppMonitorTransform extends Transform {

    @Override
    String getName() {
        return "WeMonitor"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    Map<String, ArrayList<String>> mAll = new HashMap<>()
    Map<String, File> mAllFile = new HashMap<>()

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        boolean isLeft = File.separator == '/'

        transformInvocation.inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput directoryInput ->
                String parentPath = directoryInput.file.absolutePath
                if (!parentPath.endsWith(File.separator)) {
                    parentPath += File.separator
                }
                File outFIle = outputProvider.getContentLocation(directoryInput.name, directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY)
                directoryInput.file.eachFileRecurse { File childFile ->
                    String childFilePath = childFile.absolutePath
                    if (childFilePath.endsWith(".class")) {
                        childFilePath = childFilePath.replace(parentPath, "")
                        //查找到所有的Activity
                        if (childFilePath.startsWith("cn" + File.separator + "wang") && childFilePath.endsWith("Activity.class")) {
                            ArrayList<String> fileList = mAll.get(outFIle.getName())
                            if (fileList == null) {
                                fileList = new ArrayList<>()
                            }
                            mAll.put(outFIle.name, fileList)
                            fileList.add(childFile.name)
                            printer("outFIle file name  " + outFIle.name)
                            mAllFile.put(outFIle.name, outFIle)
                        }
                    }
                }
                FileUtils.copyDirectory(directoryInput.file, outFIle)
            }

            input.jarInputs.each { JarInput jarInput ->
                File originFile = jarInput.file
                File outJarFile = outputProvider.getContentLocation(jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR)
                FileUtils.copyFile(originFile, outJarFile)
            }
        }

        if (mAllFile.size() > 0) {
            Set<String> fileKeys = mAllFile.keySet()
            fileKeys.each { String dirPath ->
                File targetFile = mAllFile.get(dirPath)
                if (null != targetFile) {
                    ArrayList<String> values = mAll.get(targetFile.name)
                    if (values != null) {
                        targetFile.eachFileRecurse { File child ->
                            if (values.contains(child.name)) {
                                String childFilePath = child.absolutePath
                                String name = childFilePath.substring(childFilePath.lastIndexOf(File.separator) + 1, childFilePath.lastIndexOf(".class"))
                                printer("Target file is  " + child.absolutePath + "   name  is  " + name)
                                scanClass(new FileInputStream(child))
                            }
                        }
                    }
                }

            }

        }

    }

    private static byte[] scanClass(InputStream inputStream) {
        ClassReader classReader = new ClassReader(inputStream)
        ClassWriter classWriter = new ClassWriter(classReader, 0)
        CusClassVisitor cusMethodVisitor = new CusClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(cusMethodVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    static class CusClassVisitor extends ClassVisitor {

        String activityName;

        CusClassVisitor(int i, ClassVisitor classVisitor) {
            super(i, classVisitor)
        }

        @Override
        void visit(int i, int i1, String s, String s1, String s2, String[] strings) {
            super.visit(i, i1, s, s1, s2, strings)
            activityName = s
        }
        /**
         *
         * @param i class的版本号。
         * @param i1 Class的访问标识。
         * @param s 类名字
         * @param s1 class 签名
         * @param s2 父类的名字
         * @param strings 接口名
         */
        @Override
        MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
            MethodVisitor mv = super.visitMethod(i, s, s1, s2, strings)
            if (s == "onWindowFocusChanged") {
                mv = new CusMethodVisitor(api, mv, activityName)
            }
            return mv
        }
    }

    static class CusMethodVisitor extends MethodVisitor {

        final String visitClass
        final String visitMethod
        final String methodName
        String activityName;

        CusMethodVisitor(int i, MethodVisitor methodVisitor, String activityName,String methodName) {
            super(i, methodVisitor)
            this.activityName = activityName
            this.methodName = methodName
            visitClass = "com.wang.monitor.monitors.OverallActivityMonitor".replace(".", File.separator)
            visitMethod = "(Ljava/lang/string;)V".replace(".", File.separator)
        }

        @Override
        void visitInsn(int i) {
            super.visitInsn(i)
            if (activityName != null && visitClass != null) {
                println("---------开始修改")
                if (i >= Opcodes.IRETURN && i <= Opcodes.RETURN) {
                    mv.visitLdcInsn(activityName)
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            visitClass,
                            methodName,
                            visitMethod,
                            false
                    )
                }
            }
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }

    static void printer(String msg) {
        System.out.println(msg)
    }

}
