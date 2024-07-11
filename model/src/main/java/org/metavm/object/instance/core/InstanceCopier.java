//package org.metavm.object.instance.core;
//
//import org.metavm.object.type.Field;
//import org.metavm.object.type.rest.dto.InstanceParentRef;
//import org.metavm.util.IdentitySet;
//import org.metavm.util.Instances;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Consumer;
//
//public class InstanceCopier extends InstanceVisitor<Instance> {
//
//    private InstanceReference currentInstance;
//    private Field currentField;
//    private final IdentitySet<Instance> descendants = new IdentitySet<>();
//    private final Map<Instance, Instance> copies = new HashMap<>();
//    private final Map<Instance, List<Consumer<Instance>>> setters = new HashMap<>();
//
//    public InstanceCopier(Instance root) {
//        root.accept(new StructuralVisitor() {
//            @Override
//            public Void visitInstance(Instance instance) {
//                descendants.add(instance);
//                return null;
//            }
//
//            @Override
//            public Void visitNullInstance(NullInstance instance) {
//                return null;
//            }
//        });
//    }
//
//    @Nullable
//    private InstanceParentRef parentRef() {
//        return currentInstance != null ?
//                new InstanceParentRef(currentInstance, currentField) : null;
//    }
//
//    protected Instance getExisting(Instance instance) {
//        return null;
//    }
//
//    public void setCurrentInstance(InstanceReference currentInstance) {
//        this.currentInstance = currentInstance;
//    }
//
//    public void setCurrentField(Field currentField) {
//        this.currentField = currentField;
//    }
//
//    @Override
//    public Instance visitInstance(Instance instance) {
//        throw new UnsupportedOperationException();
//    }
//
//    public void addCopy(Instance instance, Instance copy) {
//        copies.put(instance, copy);
//        var listeners = setters.remove(instance);
//        if (listeners != null)
//            listeners.forEach(l -> l.accept(copy));
//    }
//
//    protected Instance substituteReference(Instance value) {
//        return value;
//    }
//
//    public @Nullable Instance getReference(Instance value, Consumer<Instance> setter) {
//        if (descendants.contains(value)) {
//            var copy = copies.get(value);
//            if (copy != null)
//                return copy;
//            else {
//                setters.computeIfAbsent(value, k -> new ArrayList<>()).add(setter);
//                return null;
//            }
//        } else
//            return substituteReference(value);
//    }
//
//    private Instance createDummy(Instance instance) {
//        if (instance instanceof ClassInstance classInstance)
//            return ClassInstance.allocate(classInstance.getType());
//        else if (instance instanceof ArrayInstance arrayInstance)
//            return ArrayInstance.allocate(arrayInstance.getType());
//        else
//            throw new IllegalStateException("Can not create dummy for instance: " + instance);
//    }
//
//    @Override
//    public Instance visitClassInstance(ClassInstance instance) {
//        var copy = (ClassInstance) getExisting(instance);
//        if (copy == null) {
//            copy = ClassInstanceBuilder.newBuilder(instance.getType())
//                    .sourceRef(instance.isView() ? instance.getSourceRef() : null)
//                    .build();
//            addCopy(instance, copy);
//        }
//        var oldInstance = currentInstance;
//        var oldField = currentField;
//        currentInstance = copy.getReference();
//        for (Field field : instance.getKlass().getAllFields()) {
//            var fieldValue = instance.getField(field);
//            if (fieldValue.isNull())
//                copy.initField(field, Instances.nullInstance());
//            else if (field.isChild()) {
//                currentField = field;
//                copy.initField(field, fieldValue.accept(this));
//            } else {
//                final var copyF = copy;
//                var ref = getReference(fieldValue, i -> copyF.initField(field, i));
//                if (ref != null)
//                    copy.initField(field, ref);
//            }
//        }
//        currentInstance = oldInstance;
//        currentField = oldField;
//        return copy;
//    }
//
//    @Override
//    public Instance visitArrayInstance(ArrayInstance instance) {
//        var copy = (ArrayInstance) getExisting(instance);
//        if (copy == null) {
//            copy = new ArrayInstance(instance.getType(), parentRef());
//            addCopy(instance, copy);
//        }
//        if (instance.isChildArray()) {
//            var oldInstance = currentInstance;
//            var oldField = currentField;
//            currentInstance = copy.getReference();
//            currentField = null;
//            for (Instance element : instance) {
//                copy.addElement(element.accept(this));
//            }
////            instance.acceptChildren(this);
//            currentInstance = oldInstance;
//            currentField = oldField;
//        } else {
//            int i = 0;
//            for (Instance element : instance) {
//                final int _i = i++;
//                final var _copy = copy;
//                var ref = getReference(element, value -> _copy.setElementDirectly(_i, value));
//                if (ref == null)
//                    ref = createDummy(element);
//                copy.addElement(ref);
//            }
//        }
//        return copy;
//    }
//
//}
