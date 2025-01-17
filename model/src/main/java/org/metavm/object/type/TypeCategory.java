package org.metavm.object.type;

import org.metavm.entity.natives.ArrayNative;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.type.rest.dto.TypeCategoryCodes;
import org.metavm.system.RegionConstants;
import org.metavm.system.RegionInfo;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum TypeCategory {
    CLASS(TypeCategoryCodes.CLASS, ColumnKind.REFERENCE, 2),
    ENUM(TypeCategoryCodes.ENUM, ColumnKind.REFERENCE, 2),
    INTERFACE(TypeCategoryCodes.INTERFACE, ColumnKind.UNSPECIFIED, 3),
    VALUE(TypeCategoryCodes.VALUE, ColumnKind.UNSPECIFIED, 2 ),
    READ_WRITE_ARRAY(TypeCategoryCodes.READ_WRITE_ARRAY, ColumnKind.REFERENCE, ArrayNative.class, 4),
    READ_ONLY_ARRAY(TypeCategoryCodes.READ_ONLY_ARRAY, ColumnKind.REFERENCE, ArrayNative.class, 4),
    CHILD_ARRAY(TypeCategoryCodes.CHILD_ARRAY, ColumnKind.REFERENCE, ArrayNative.class, 4),
    VALUE_ARRAY(TypeCategoryCodes.VALUE_ARRAY, ColumnKind.REFERENCE, ArrayNative.class, 4),
    UNION(TypeCategoryCodes.UNION, ColumnKind.UNSPECIFIED, 5),
    NULL(TypeCategoryCodes.NULL, ColumnKind.UNSPECIFIED, 5),
    STRING(TypeCategoryCodes.STRING, ColumnKind.STRING, 5),
    DOUBLE(TypeCategoryCodes.DOUBLE, ColumnKind.DOUBLE, 5),
    LONG(TypeCategoryCodes.LONG, ColumnKind.INT, 5),
    BOOLEAN(TypeCategoryCodes.BOOLEAN, ColumnKind.BOOL, 5),
    TIME(TypeCategoryCodes.TIME, ColumnKind.INT, 5),
    ANY(TypeCategoryCodes.OBJECT, ColumnKind.UNSPECIFIED, 5),
    PASSWORD(TypeCategoryCodes.PASSWORD, ColumnKind.STRING, 5),
    VOID(TypeCategoryCodes.VOID, ColumnKind.UNSPECIFIED, 5),
    CHAR(TypeCategoryCodes.CHAR, ColumnKind.STRING, 5),
    VARIABLE(TypeCategoryCodes.VARIABLE, ColumnKind.UNSPECIFIED, 1),
    INTERSECTION(TypeCategoryCodes.INTERSECTION, ColumnKind.UNSPECIFIED, 4),
    FUNCTION(TypeCategoryCodes.FUNCTION, ColumnKind.UNSPECIFIED, 5),
    UNCERTAIN(TypeCategoryCodes.UNCERTAIN, ColumnKind.UNSPECIFIED, 5),
    NEVER(TypeCategoryCodes.NOTHING, ColumnKind.UNSPECIFIED, 5),
    CAPTURED(TypeCategoryCodes.CAPTURED, ColumnKind.UNSPECIFIED, 5),
    INT(TypeCategoryCodes.INT, ColumnKind.INT, 5),
    FLOAT(TypeCategoryCodes.FLOAT, ColumnKind.DOUBLE, 5),
    SHORT(TypeCategoryCodes.SHORT, ColumnKind.INT, 5),
    BYTE(TypeCategoryCodes.BYTE, ColumnKind.INT, 5);

    private final int code;
    private final int closurePrecedence;
    private final ColumnKind columnKind;
    @Nullable
    private final Class<?> nativeClass;

    private static volatile Set<TypeCategory> ALL;

    TypeCategory(int code, ColumnKind columnKind, int closurePrecedence) {
        this(code, columnKind, null, closurePrecedence);
    }

    TypeCategory(int code, ColumnKind columnKind, @Nullable Class<?> nativeClass, int closurePrecedence) {
        this.code = code;
        this.columnKind = columnKind;
        this.nativeClass = nativeClass;
        this.closurePrecedence = closurePrecedence;
    }

    public static TypeCategory fromCode(int code) {
        return Arrays.stream(values()).filter(t -> t.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Can not find type category fo：code: " + code));
    }

    public static List<TypeCategory> arrayCategories() {
        return List.of(READ_WRITE_ARRAY, READ_ONLY_ARRAY, CHILD_ARRAY);
    }

    public static Set<TypeCategory> pojoCategories() {
        return Set.of(TypeCategory.CLASS, TypeCategory.VALUE, TypeCategory.INTERFACE);
    }

    public int code() {
        return code;
    }

    public boolean isClass() {
        return this == CLASS;
    }

    public boolean isEnum() {
        return this == ENUM;
    }

    public boolean isNull() {
        return this == NULL;
    }

    public boolean isValue() {
        return this == VALUE;
    }

    public boolean isArray() {
        return this == READ_ONLY_ARRAY || this == READ_WRITE_ARRAY || this == CHILD_ARRAY;
    }

    public boolean isObject() {
        return this == ANY;
    }

    public boolean isPrimitive() {
        return isString() || isTime() || isDouble()
                || isLong() || isBool() || isPassword() || isNull();
    }

    public boolean idRangeContains(long id) {
        return Utils.mapOrElse(getIdRegion(), r -> r.contains(id), () -> false);
    }

    public @Nullable RegionInfo getIdRegion() {
        return RegionConstants.getRegionStatic(this);
    }

    public boolean isString() {
        return this == STRING;
    }

    public boolean isDouble() {
        return this == DOUBLE;
    }

    public boolean isLong() {
        return this == LONG;
    }

    public boolean isPassword() {
        return this == PASSWORD;
    }

    @SuppressWarnings("unused")
    public boolean isUnion() {
        return this == UNION;
    }

    public boolean isVariable() {
        return this == VARIABLE;
    }

    public boolean isTime() {
        return this == TIME;
    }

    public boolean isBool() {
        return this == BOOLEAN;
    }

    public boolean isEntity() {
        return this == CLASS;
    }

    public ColumnKind getSQLType() {
        return columnKind;
    }

    public boolean isInterface() {
        return this == INTERFACE;
    }

    public @Nullable Class<?> getNativeClass() {
        return nativeClass;
    }

    public boolean isPojo() {
        return this == INTERFACE || this == CLASS || this == ENUM;
    }

    public boolean isFunction() {
        return this == FUNCTION;
    }

    public boolean isUncertain() {
        return this == UNCERTAIN;
    }

    public boolean isIntersection() {
        return this == INTERSECTION;
    }

    public static Set<TypeCategory> all() {
        if(ALL == null) {
            ALL = Set.of(values());
        }
        return ALL;
    }

    public int closurePrecedence() {
        return closurePrecedence;
    }

}
