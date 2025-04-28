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

    public static final Map<String, Integer> map = Map.<String, Integer>ofEntries(
            Map.entry("Method", TAG_Method),
            Map.entry("CapturedTypeVariable", TAG_CapturedTypeVariable),
            Map.entry("Application", TAG_Application),
            Map.entry("ListView", TAG_ListView),
            Map.entry("PlatformMessage", TAG_PlatformMessage),
            Map.entry("StaticFieldTableEntry", TAG_StaticFieldTableEntry),
            Map.entry("RemoveAppTaskGroup", TAG_RemoveAppTaskGroup),
            Map.entry("KlassSourceCodeTagAssigner", TAG_KlassSourceCodeTagAssigner),
            Map.entry("SystemDDL", TAG_SystemDDL),
            Map.entry("DynamicTaskGroup", TAG_DynamicTaskGroup),
            Map.entry("ComponentBeanDefinition", TAG_ComponentBeanDefinition),
            Map.entry("AddFieldTask", TAG_AddFieldTask),
            Map.entry("Commit", TAG_Commit),
            Map.entry("TypeVariable", TAG_TypeVariable),
            Map.entry("User", TAG_User),
            Map.entry("Session", TAG_Session),
            Map.entry("FactoryBeanDefinition", TAG_FactoryBeanDefinition),
            Map.entry("AppInvitation", TAG_AppInvitation),
            Map.entry("SendMessageTask", TAG_SendMessageTask),
            Map.entry("TypeDef", TAG_TypeDef),
            Map.entry("SessionEntry", TAG_SessionEntry),
            Map.entry("ReferenceRedirector", TAG_ReferenceRedirector),
            Map.entry("ExecutorData", TAG_ExecutorData),
            Map.entry("DummyTypeVariable", TAG_DummyTypeVariable),
            Map.entry("Klass", TAG_Klass),
            Map.entry("ChangeLog", TAG_ChangeLog),
            Map.entry("Flow", TAG_Flow),
            Map.entry("ScanByClassTask", TAG_ScanByClassTask),
            Map.entry("ClearUsersTask", TAG_ClearUsersTask),
            Map.entry("IndexRebuildGlobalTask", TAG_IndexRebuildGlobalTask),
            Map.entry("CheckConstraint", TAG_CheckConstraint),
            Map.entry("Task", TAG_Task),
            Map.entry("SchedulerRegistry", TAG_SchedulerRegistry),
            Map.entry("GlobalTask", TAG_GlobalTask),
            Map.entry("Index", TAG_Index),
            Map.entry("DDLRollbackTask", TAG_DDLRollbackTask),
            Map.entry("SimpleDDLTask", TAG_SimpleDDLTask),
            Map.entry("PreUpgradeTask", TAG_PreUpgradeTask),
            Map.entry("GlobalKlassTagAssigner", TAG_GlobalKlassTagAssigner),
            Map.entry("IndexRebuildTask", TAG_IndexRebuildTask),
            Map.entry("ReferenceCleanupTask", TAG_ReferenceCleanupTask),
            Map.entry("ClearInvitationTask", TAG_ClearInvitationTask),
            Map.entry("ForwardedFlagSetter", TAG_ForwardedFlagSetter),
            Map.entry("ShadowTask", TAG_ShadowTask),
            Map.entry("ReferenceScanner", TAG_ReferenceScanner),
            Map.entry("DDLTask", TAG_DDLTask),
            Map.entry("PlatformUser", TAG_PlatformUser),
            Map.entry("EagerFlagSetter", TAG_EagerFlagSetter),
            Map.entry("ScanByTypeTask", TAG_ScanByTypeTask),
            Map.entry("VerificationCode", TAG_VerificationCode),
            Map.entry("ScanTask", TAG_ScanTask),
            Map.entry("BeanDefinitionRegistry", TAG_BeanDefinitionRegistry),
            Map.entry("GlobalPreUpgradeTask", TAG_GlobalPreUpgradeTask),
            Map.entry("CloseAllSessionsTask", TAG_CloseAllSessionsTask),
            Map.entry("Message", TAG_Message),
            Map.entry("LoginAttempt", TAG_LoginAttempt),
            Map.entry("StaticFieldTable", TAG_StaticFieldTable),
            Map.entry("KlassFlags", TAG_KlassFlags),
            Map.entry("Parameter", TAG_Parameter),
            Map.entry("BeanDefinition", TAG_BeanDefinition),
            Map.entry("Role", TAG_Role),
            Map.entry("KlassTagAssigner", TAG_KlassTagAssigner),
            Map.entry("TaskGroup", TAG_TaskGroup),
            Map.entry("Lambda", TAG_Lambda),
            Map.entry("Function", TAG_Function),
            Map.entry("PublishMetadataEventTask", TAG_PublishMetadataEventTask),
            Map.entry("Constraint", TAG_Constraint),
            Map.entry("WAL", TAG_WAL),
            Map.entry("AddFieldTaskGroup", TAG_AddFieldTaskGroup),
            Map.entry("Field", TAG_Field),
            Map.entry("SynchronizeSearchTask", TAG_SynchronizeSearchTask),
            Map.entry("AttributedElement", TAG_AttributedElement),
            Map.entry("EntityScanTask", TAG_EntityScanTask),
            Map.entry("Version", TAG_Version),
            Map.entry("DummyAny", TAG_DummyAny),
            Map.entry("DDLRollbackTaskGroup", TAG_DDLRollbackTaskGroup),
            Map.entry("AstExceptionFoo", TAG_AstExceptionFoo),
            Map.entry("TypeReducerFoo", TAG_TypeReducerFoo),
            Map.entry("AstBranchFoo", TAG_AstBranchFoo),
            Map.entry("UpgradeValue", TAG_UpgradeValue),
            Map.entry("LivingBeing", TAG_LivingBeing),
            Map.entry("Human", TAG_Human),
            Map.entry("ValueFoo", TAG_ValueFoo),
            Map.entry("Bar", TAG_Bar),
            Map.entry("Animal", TAG_Animal),
            Map.entry("AstDirectCoupon", TAG_AstDirectCoupon),
            Map.entry("AstOrder", TAG_AstOrder),
            Map.entry("AstProduct", TAG_AstProduct),
            Map.entry("EntityFoo", TAG_EntityFoo),
            Map.entry("Qux", TAG_Qux),
            Map.entry("IndexFoo", TAG_IndexFoo),
            Map.entry("UpgradeSingleton", TAG_UpgradeSingleton),
            Map.entry("UpgradeBar", TAG_UpgradeBar),
            Map.entry("Foo", TAG_Foo),
            Map.entry("Baz", TAG_Baz),
            Map.entry("UpgradeFoo", TAG_UpgradeFoo),
            Map.entry("TestTask", TAG_TestTask),
            Map.entry("AstSimpleProduct", TAG_AstSimpleProduct),
            Map.entry("EntityBar", TAG_EntityBar)
    );

}
