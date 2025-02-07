package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethodReferenceExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiTypeParameter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Index;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.util.CompilerException;
import org.metavm.util.LinkedList;
import org.metavm.util.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
public class IndexCreator extends VisitorBase {

    private final TypeResolver typeResolver;
    private final LinkedList<ClassInfo> classes = new LinkedList<>();

    public IndexCreator(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if (!(aClass instanceof PsiTypeParameter)) {
            var klass = Objects.requireNonNull(aClass.getUserData(Keys.MV_CLASS),
                    () -> "Cannot find MetaVM class for " + TranspileUtils.getQualifiedName(aClass));
            var classInfo = new ClassInfo(klass);
            classes.push(classInfo);
            super.visitClass(aClass);
            classes.pop();
            Utils.exclude(klass.getIndices(), classInfo.visitedIndexes::contains).forEach(klass::removeConstraint);
        }
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (typeResolver.resolveDeclaration(requireNonNull(expression.getType())) instanceof KlassType classType
                && classType.getKlass() == StdKlass.index.get()) {
            var valueKlass = ((ClassType) classType.getTypeArguments().get(1)).getKlass();
            var classInfo = currentClassInfo();
            if (valueKlass != classInfo.klass)
                throw new CompilerException("Cannot define index outside of the value class: " + expression.getText());
            var args = requireNonNull(expression.getArgumentList()).getExpressions();
            var name = (String) TranspileUtils.getConstant(args[0]);
            var index = valueKlass.findSelfIndex(i -> i.getName().equals(name));
            var keyType = classType.getTypeArguments().getFirst();
            if(index == null)
                index = new Index(valueKlass, name, "", TranspileUtils.isUniqueIndex(expression), keyType, null);
            classInfo.visitedIndexes.add(index);
            var methodRef = (PsiMethodReferenceExpression) args[2];
            index.setMethod(requireNonNull(requireNonNull(methodRef.resolve()).getUserData(Keys.Method)));
        }
    }

    private ClassInfo currentClassInfo() {
        return Objects.requireNonNull(classes.peek());
    }

    private static class ClassInfo {
        private final Klass klass;
        private final Set<Index> visitedIndexes = new HashSet<>();

        private ClassInfo(Klass klass) {
            this.klass = klass;
        }
    }

}
