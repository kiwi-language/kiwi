package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRTypeUtil;
import tech.metavm.util.ReflectUtils;

public record IRPackage(String name) {

    public static final IRPackage ROOT_PKG = new IRPackage("");

    public IRClass tryGetClass(String name) {
        Class<?> klass = ReflectUtils.tryClassForName(getClassName(name));
        return klass != null ? IRTypeUtil.fromClass(klass) : null;
    }

    public String getClassName(String simpleName) {
        return simpleName != null ? name.equals("") ? simpleName : name + "." + simpleName : null;
    }

    public IRPackage subPackage(String name) {
        return new IRPackage(this.name + "." + name);
    }


}
