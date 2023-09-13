package me.threedr3am.log.agent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author threedr3am
 */
public class CatClassFileTransformer implements ClassFileTransformer {

    private String pkgPattern = ".";
    private Pattern pattern;

    public String getPkgPattern() {
        return pkgPattern;
    }

    public void setPkgPattern(String pkgPattern) {
        this.pkgPattern = pkgPattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (pattern == null) {
            pattern = Pattern.compile(pkgPattern);
        }
        className = className.replace("/", ".");
        if (pattern.matcher(className).find()) {
            System.out.println("[LOG-AGENT] --------- modify class: " + className);
            CtClass ctClass = null;
            try {
                ClassPool classPool = ClassPool.getDefault();
                addLoader(classPool, loader);
                ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                Set<String> cache = new HashSet();
                CtMethod[] ctMethods = ctClass.getMethods();
                if (ctMethods != null) {
                    inject(ctMethods, cache);
                }
                ctMethods = ctClass.getDeclaredMethods();
                if (ctMethods != null) {
                    inject(ctMethods, cache);
                }
                return ctClass.toBytecode();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (ctClass != null) {
                    ctClass.detach();
                }
            }
            System.out.println("[LOG-AGENT] --------- modify class end.");
        }
        return classfileBuffer;
    }

    /**
     * 为方法注入Hook
     *
     * @param ctMethods
     * @param cache
     */
    private void inject(CtMethod[] ctMethods, Set<String> cache) {
        for (int i = 0; i < ctMethods.length; i++) {
            CtMethod ctMethod = ctMethods[i];

            if (ctMethod.isEmpty() || Modifier.isNative(ctMethod.getModifiers())) {
                continue;
            }

            String methodName = ctMethod.getLongName();
            if (cache.contains(methodName)) {
                continue;
            }

            // 在每个方法的方法体头部都加上调用自己的检查调用的代码，那么这个检查又检查了什么呢？
            try {
                System.out.println("[LOG-AGENT]           method: " + methodName + " " + cache.size());
                StringBuilder stringBuilder = new StringBuilder()
                        .append("{")
                        .append(String.format("   if (me.threedr3am.log.agent.CatContext.check(\"%s\"))", methodName))
                        .append(String.format("         System.out.println(\"%s %s\");", "[LOG-AGENT] ", methodName))
                        .append("}");
                ctMethod.insertBefore(stringBuilder.toString());
                cache.add(methodName);
            } catch (Throwable e) {
                System.err.printf("[LOG-AGENT] inject code into method:%s fail!%n", methodName);
                e.printStackTrace();
            }
        }
    }

    private void addLoader(ClassPool classPool, ClassLoader loader) {
        classPool.appendSystemPath();
        classPool.appendClassPath(new ClassClassPath(CatClassFileTransformer.class));
        if (loader != null) {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }
    }
}
