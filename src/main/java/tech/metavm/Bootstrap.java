package tech.metavm;

import javassist.*;

public class Bootstrap {

    public static void main(String[] args) throws Throwable {
        ClassLoader appClassLoader = Bootstrap.class.getClassLoader();
        ClassPool pool = ClassPool.getDefault();
        Loader loader = new Loader(appClassLoader, pool);
        pool.appendClassPath(new LoaderClassPath(appClassLoader));
        loader.delegateLoadingOf("jdk.");
        loader.delegateLoadingOf("org.");
        loader.delegateLoadingOf("net.");
        loader.delegateLoadingOf("com.");
        loader.addTranslator(pool, new Translator() {
            public void start(ClassPool pool) {}

            public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
                if(!classname.startsWith("tech.metavm.") || classname.contains("$$")) {
                    return;
                }
                CtClass cc = pool.get(classname);
                if(!isEntity(cc)) {
                    return;
                }
                CtConstructor[] constructors = cc.getConstructors();
                for (CtConstructor constructor : constructors) {
                    enhanceEntityConstructor(constructor);
                }
            }
        });
        Thread.currentThread().setContextClassLoader(loader);
        loader.run("tech.metavm.ObjectApplication", new String[0]);
    }

    private static void enhanceEntityConstructor(CtConstructor constructor) throws CannotCompileException {
        constructor.insertAfter("this.bind();");
    }

    private static boolean isEntity(CtClass cc) throws NotFoundException {
        CtClass sc = cc.getSuperclass();
        while(sc != null && !sc.getName().equals("java.lang.Object")) {
            if(sc.getName().equals("tech.metavm.entity.Entity")) {
                return true;
            }
            sc = sc.getSuperclass();
        }
        return false;
    }

}
