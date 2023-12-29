package tech.metavm.object.view;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.flow.Value;
import tech.metavm.flow.*;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.view.rest.dto.ArrayMappingDTO;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NamingUtils.escapeTypeName;

@EntityType("数组映射")
public class ArrayMapping extends Mapping implements GlobalKey {

    public static final IndexDef<ArrayMapping> IDX =
            IndexDef.createUnique(ArrayMapping.class, "sourceType", "targetType", "elementMapping");

    @EntityField("元素映射")
    @Nullable
    private final Mapping elementMapping;

    public ArrayMapping(@Nullable Long tmpId, ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping) {
        super(tmpId, getName(sourceType, targetType, elementMapping), getCode(sourceType, targetType, elementMapping), sourceType, targetType);
        if (elementMapping != null) {
            AssertUtils.assertTrue(
                    elementMapping.getSourceType().isAssignableFrom(sourceType.getElementType())
                            && targetType.getElementType().isAssignableFrom(elementMapping.getTargetType()),
                    ErrorCode.INCORRECT_ARRAY_MAPPING_ARGUMENTS
            );
        }
        this.elementMapping = elementMapping;
    }

    private static String getName(ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping) {
        if (elementMapping != null)
            return escapeTypeName(sourceType.getName() + "_" + targetType.getName() + "_" + elementMapping.getName());
        else
            return escapeTypeName(sourceType.getName() + "_" + targetType.getName());
    }

    private static @Nullable String getCode(ArrayType sourceType, ArrayType targetType, @Nullable Mapping elementMapping) {
        if (sourceType.getCode() != null && targetType.getCode() != null
                && (elementMapping == null || elementMapping.getCode() != null)) {
            if (elementMapping != null)
                return escapeTypeName(sourceType.getCode() + "_" + targetType.getCode() + "_" + elementMapping.getCode());
            else
                return escapeTypeName(sourceType.getCode() + "_" + targetType.getCode());
        } else
            return null;
    }

    @Override
    public Flow generateMappingCode(FunctionTypeProvider functionTypeProvider) {
        var scope = Objects.requireNonNull(mapper).getRootScope();
        scope.clearNodes();
        var input = Nodes.input(mapper);
        var view = new NewArrayNode(null, "视图", "View", getTargetType(),
                null, null, scope.getLastNode(), scope);
        Nodes.setSource(Values.node(view), Values.inputValue(input, 0), scope);
        Nodes.forEach(
                () -> Values.inputValue(input, 0),
                (loopBody, elementSupplier, indexSupplier) -> {
                    Value viewElementValue;
                    if (elementMapping != null) {
                        var viewElement = new FunctionCallNode(
                                null,
                                "视图元素",
                                "ViewElement",
                                loopBody.getLastNode(),
                                loopBody,
                                elementMapping.getMapper(),
                                List.of(Nodes.argument(elementMapping.getMapper(), 0, elementSupplier.get()))
                        );
                        viewElementValue = Values.node(viewElement);
                    } else
                        viewElementValue = elementSupplier.get();
                    new AddElementNode(null, "添加视图元素", "AddViewElement", loopBody.getLastNode(),
                            loopBody, Values.node(view), viewElementValue);
                },
                scope
        );
        new ReturnNode(null, "返回", "Return", scope.getLastNode(), scope, Values.node(view));
        return mapper;
    }

    @Override
    public Flow generateUnmappingCode(FunctionTypeProvider functionTypeProvider) {
        var scope = Objects.requireNonNull(unmapper).getRootScope();
        scope.clearNodes();
        var input = Nodes.input(unmapper);
        var inputViewField = input.getType().getFields().get(0);
        var getSourceFunc = NativeFunctions.getSource();
        var source = new FunctionCallNode(null, "来源", "Source", input, scope,
                getSourceFunc, List.of(Nodes.argument(getSourceFunc, 0, Values.nodeProperty(input, inputViewField)))
        );
        var castedSource = new CastNode(null, "Casted来源", "CastedSource", getSourceType(),
                scope.getLastNode(), scope, Values.node(source));
        new ClearArrayNode(null, "清空来源数组", "ClearSourceArray", scope.getLastNode(), scope,
                Values.node(castedSource));
        Nodes.forEach(
                () -> Values.nodeProperty(input, inputViewField),
                (bodyScope, elementSupplier, indexSupplier) -> {
                    Value sourceElementValue;
                    if (elementMapping != null) {
                        var sourceElement = new FunctionCallNode(
                                null,
                                "来源数组元素",
                                "SourceElement",
                                bodyScope.getLastNode(),
                                bodyScope,
                                elementMapping.getUnmapper(),
                                List.of(Nodes.argument(elementMapping.getUnmapper(), 0, elementSupplier.get()))
                        );
                        sourceElementValue = Values.node(sourceElement);
                    } else
                        sourceElementValue = elementSupplier.get();
                    new AddElementNode(null, "添加来源数组元素", "AddSourceElement", bodyScope.getLastNode(), bodyScope,
                            Values.node(castedSource), sourceElementValue);
                },
                scope
        );
        new ReturnNode(null, "结束", "Return", scope.getLastNode(), scope, Values.node(castedSource));
        return unmapper;
    }

    @Override
    public ArrayType getSourceType() {
        return (ArrayType) super.getSourceType();
    }

    @Override
    public ArrayType getTargetType() {
        return (ArrayType) super.getTargetType();
    }

    @Override
    public ArrayMappingDTO toDTO(SerializeContext context) {
        return new ArrayMappingDTO(
                id,
                context.getTmpId(this),
                context.getRef(sourceType),
                context.getRef(targetType),
                NncUtils.get(elementMapping, context::getRef)
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayMapping(this);
    }

    @Nullable
    public Mapping getElementMapping() {
        return elementMapping;
    }

    @Override
    public boolean isValidGlobalKey() {
        return sourceType.isValidGlobalKey() && targetType.isValidGlobalKey()
                && (elementMapping instanceof LocalKey localKey && localKey.isValidLocalKey() ||
                elementMapping instanceof GlobalKey globalKey && globalKey.isValidGlobalKey());
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return context.getModelName(sourceType, this) + '_'
                + context.getModelName(elementMapping, this) + '_'
                + context.getModelName(targetType, this);
    }
}
