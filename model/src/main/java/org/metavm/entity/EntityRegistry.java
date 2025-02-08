package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.StreamVisitor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class EntityRegistry {

    public static final Class<?>[] classes = new Class[255];
    private static final Map<String, Class<?>> name2class = new HashMap<>();
    public static final MethodHandle[] visitBodyHandles = new MethodHandle[255];

    static {
        var klasses = EntityUtils.getModelClasses();
        for (Class<?> clazz : klasses) {
            var entity = clazz.getAnnotation(NativeEntity.class);
            int treeTag;
            if (entity != null && (treeTag = entity.value()) != -1) {
                var visitHandle = ReflectionUtils.getMethodHandle(MethodHandles.lookup(), clazz,
                        "visitBody", void.class, List.of(StreamVisitor.class), true);
                visitBodyHandles[treeTag] = visitHandle;
                classes[treeTag] = clazz;
                name2class.put(clazz.getName(), clazz);
            }
        }
    }

    public static Class<? extends org.metavm.entity.Entity> getEntityClass(int treeTag) {
        //noinspection unchecked
        return (Class<? extends org.metavm.entity.Entity>) classes[treeTag];
    }

    public static Class<?> getClassByName(String name) {
        return Objects.requireNonNull(name2class.get(name), () -> "Cannot find class for name '" + name + "'");
    }

    public static MethodHandle getVisitBodyHandle(int treeTag) {
        return visitBodyHandles[treeTag];
    }

// Generated. Do not modify. See generate_entity_registry.groovy
    public static final int TAG_Method = 1;
    public static final int TAG_CapturedTypeVariable = 2;
    public static final int TAG_Application = 3;
    public static final int TAG_ListView = 4;
    public static final int TAG_PlatformMessage = 5;
    public static final int TAG_StaticFieldTableEntry = 6;
    public static final int TAG_RemoveAppTaskGroup = 7;
    public static final int TAG_KlassSourceCodeTagAssigner = 8;
    public static final int TAG_SystemDDL = 9;
    public static final int TAG_DynamicTaskGroup = 10;
    public static final int TAG_ComponentBeanDefinition = 11;
    public static final int TAG_AddFieldTask = 12;
    public static final int TAG_Commit = 13;
    public static final int TAG_TypeVariable = 15;
    public static final int TAG_User = 16;
    public static final int TAG_Session = 17;
    public static final int TAG_FactoryBeanDefinition = 18;
    public static final int TAG_AppInvitation = 19;
    public static final int TAG_SendMessageTask = 20;
    public static final int TAG_TypeDef = 21;
    public static final int TAG_SessionEntry = 22;
    public static final int TAG_ReferenceRedirector = 23;
    public static final int TAG_ExecutorData = 24;
    public static final int TAG_DummyTypeVariable = 25;
    public static final int TAG_Klass = 26;
    public static final int TAG_ChangeLog = 27;
    public static final int TAG_Flow = 28;
    public static final int TAG_ScanByClassTask = 29;
    public static final int TAG_ClearUsersTask = 30;
    public static final int TAG_IndexRebuildGlobalTask = 31;
    public static final int TAG_CheckConstraint = 32;
    public static final int TAG_Task = 33;
    public static final int TAG_SchedulerRegistry = 34;
    public static final int TAG_GlobalTask = 35;
    public static final int TAG_Index = 36;
    public static final int TAG_DDLRollbackTask = 37;
    public static final int TAG_SimpleDDLTask = 38;
    public static final int TAG_PreUpgradeTask = 39;
    public static final int TAG_GlobalKlassTagAssigner = 40;
    public static final int TAG_IndexRebuildTask = 41;
    public static final int TAG_ReferenceCleanupTask = 42;
    public static final int TAG_ClearInvitationTask = 43;
    public static final int TAG_ForwardedFlagSetter = 44;
    public static final int TAG_ShadowTask = 45;
    public static final int TAG_ReferenceScanner = 46;
    public static final int TAG_DDLTask = 47;
    public static final int TAG_PlatformUser = 48;
    public static final int TAG_EagerFlagSetter = 49;
    public static final int TAG_ScanByTypeTask = 50;
    public static final int TAG_VerificationCode = 51;
    public static final int TAG_ScanTask = 52;
    public static final int TAG_BeanDefinitionRegistry = 53;
    public static final int TAG_GlobalPreUpgradeTask = 54;
    public static final int TAG_CloseAllSessionsTask = 55;
    public static final int TAG_Message = 56;
    public static final int TAG_LoginAttempt = 57;
    public static final int TAG_StaticFieldTable = 58;
    public static final int TAG_KlassFlags = 59;
    public static final int TAG_Parameter = 60;
    public static final int TAG_BeanDefinition = 62;
    public static final int TAG_Role = 63;
    public static final int TAG_KlassTagAssigner = 64;
    public static final int TAG_TaskGroup = 65;
    public static final int TAG_Lambda = 66;
    public static final int TAG_Function = 67;
    public static final int TAG_PublishMetadataEventTask = 68;
    public static final int TAG_Constraint = 69;
    public static final int TAG_WAL = 70;
    public static final int TAG_AddFieldTaskGroup = 71;
    public static final int TAG_Field = 72;
    public static final int TAG_SynchronizeSearchTask = 73;
    public static final int TAG_AttributedElement = 74;
    public static final int TAG_EntityScanTask = 75;
    public static final int TAG_Version = 76;
    public static final int TAG_DummyAny = 77;
    public static final int TAG_DDLRollbackTaskGroup = 78;
    public static final int TAG_AstExceptionFoo = 79;
    public static final int TAG_TypeReducerFoo = 80;
    public static final int TAG_AstBranchFoo = 81;
    public static final int TAG_UpgradeValue = 82;
    public static final int TAG_LivingBeing = 83;
    public static final int TAG_Human = 84;
    public static final int TAG_ValueFoo = 85;
    public static final int TAG_Bar = 86;
    public static final int TAG_Animal = 87;
    public static final int TAG_AstDirectCoupon = 88;
    public static final int TAG_AstOrder = 89;
    public static final int TAG_AstProduct = 90;
    public static final int TAG_EntityFoo = 91;
    public static final int TAG_Qux = 92;
    public static final int TAG_IndexFoo = 93;
    public static final int TAG_UpgradeSingleton = 94;
    public static final int TAG_UpgradeBar = 95;
    public static final int TAG_Foo = 96;
    public static final int TAG_Baz = 97;
    public static final int TAG_UpgradeFoo = 98;
    public static final int TAG_TestTask = 99;
    public static final int TAG_AstSimpleProduct = 100;
    public static final int TAG_EntityBar = 101;
}
