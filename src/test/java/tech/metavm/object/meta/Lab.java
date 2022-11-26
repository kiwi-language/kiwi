package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.util.Column;

import java.lang.reflect.*;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Lab {

    private final long value;

    public Lab(long value) {
        this.value = value;
    }

    public static void main(String[] args) throws Exception{
        Column column = new Column("col1", SQLColumnType.INT64);
        Field field = Column.class.getDeclaredField("name");
        field.setAccessible(true);

        Class<?>[] componentTypes = Arrays.stream(Column.class.getRecordComponents()).map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        System.out.println(Arrays.toString(Column.class.getDeclaredFields()));

        Constructor<?> constructor = Column.class.getDeclaredConstructor(componentTypes);
        System.out.println(constructor);



    }

}
