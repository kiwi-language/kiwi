package tech.metavm.autograph;


import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import java.util.*;

public class ControlFlowTransformer extends JavaRecursiveElementVisitor {

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        Scope bodyScope = statement.getUserData(Keys.BODY_SCOPE),
                elseScope = statement.getUserData(Keys.ELSE_SCOPE);

    }

    private BlockVars getBlockVars(Set<QualifiedName> modified, Set<QualifiedName> liveIn, Set<QualifiedName> liveOut) {
        Set<QualifiedName> condVars = new HashSet<>(getBasicBlockVars(modified, liveIn, liveOut));
        condVars.addAll(getCompositeBlockVars(modified, liveIn));
        Set<QualifiedName> undefined = new HashSet<>();


        return new BlockVars(condVars, undefined, 0);
    }

    private Set<QualifiedName> getBasicBlockVars(Set<QualifiedName> modified, Set<QualifiedName> liveIn, Set<QualifiedName> liveOut) {
        Set<QualifiedName> result = new HashSet<>();
        for (QualifiedName qualifiedName : modified) {
            if(qualifiedName.isComposite()) continue;
            if(liveIn.contains(qualifiedName) || liveOut.contains(qualifiedName)) result.add(qualifiedName);
        }
        return result;
    }

    private Set<QualifiedName> getCompositeBlockVars(Set<QualifiedName> modified, Set<QualifiedName> liveIn) {
        Set<QualifiedName> blockVars = new HashSet<>();
        for (QualifiedName qualifiedName : modified) {
            if(NncUtils.allMatch(qualifiedName.supportSet(), liveIn::contains)) {
                blockVars.add(qualifiedName);
            }
        }
        return blockVars;
    }

//    private PsiMethod createBlockMethod(BlockVars blockVars, PsiElement body) {
//        var varDeclMap = createVariableDeclarations(blockVars);
//        List<PsiElement> decls = new ArrayList<>();
//        for (Map.Entry<String, PsiElement> entry : varDeclMap.entrySet()) {
//            String declTemplate = """
//                    var varName = state.get(varNameLiteral)
//                    """;
//
//            decls.addAll(Templates.getInstance().replaceAsStatements(declTemplate, Map.of(
//                    "varName", List.of(
//                            Templates.getInstance().createReference(entry.getKey())
//                    ),
//                    "varNameLiteral", List.of(
//                            Templates.getInstance().createLiteral("\"" + entry.getKey() + "\"")
//                    )
//            )));
//        }
//        String template = """
//                private void bodyName() {
//                    variableDeclarations
//                    body
//                }
//                """;
//        return Templates.getInstance().replaceAsMethod(template,
//                Map.of("variableDeclarations", decls, "body", getStatements(body))
//        );
//    }

    private List<PsiElement> getStatements(PsiElement element) {
        if(element instanceof PsiBlockStatement block) {
            return Arrays.asList(block.getCodeBlock().getStatements());
        }
        else return List.of(element);
    }

    private Map<String, PsiElement> createVariableDeclarations(BlockVars blockVars) {
        return null;
    }

    public List<PsiElement> createStateMethods(BlockVars blockVars, String getterName, String setterName) {
        List<PsiElement> guardedBlockVars = new ArrayList<>();
        for (QualifiedName v : blockVars.condVars) {
            if (v.isSimple()) {
                guardedBlockVars.add(Templates.getInstance().createReference(v.toString()));
            }
            else {
                guardedBlockVars.add(Templates.getInstance().replaceAsExpression("""
                        (name) -> {
                            try {
                                return name;
                            }
                            catch(NullPointerException e) {
                                return null;
                            }
                        }
                        """, Map.of("name", Templates.getInstance().createReference(v.toString()))));
            }
        }
        String getterTemplate = """
                private Map<String, Object> getterName(StateHolder state) {
                    
                }
                """;
        var getter = Templates.getInstance().replaceAsMethod(
                getterTemplate,
                Map.of(
                        "getterName",
                        List.of(Templates.getInstance().createReference(getterName))
                )
        );
        String setterTemplate = """
                private void setterName(Map<String, Object> state) {
                
                }
                """;
        var setter = Templates.getInstance().replaceAsMethod(
                setterTemplate,
                Map.of(
                        "setterName",
                        List.of(Templates.getInstance().createReference(setterName))
                )
        );
        return List.of(getter, setter);
    }

    /*
      def _create_state_functions(
      self, block_vars, nonlocal_declarations, getter_name, setter_name):
    if not block_vars:
      templateName = """
        def getter_name():
          return ()
        def setter_name(block_vars):
          pass
      """
      return templates.replace(
          templateName, getter_name=getter_name, setter_name=setter_name)

    guarded_block_vars = []
    for v in block_vars:
      if v.is_simple():
        guarded_block_vars.append(v)
      else:
        guarded_block_vars.append(
            templates.replace_as_expression(
                'ag__.ldu(lambda: var_, name)',
                var_=v,
                name=gast.Constant(str(v), kind=None)))

    templateName = """
      def getter_name():
        return guarded_state_vars,
      def setter_name(vars_):
        nonlocal_declarations
        state_vars, = vars_
    """
    return templates.replace(
        templateName,
        nonlocal_declarations=nonlocal_declarations,
        getter_name=getter_name,
        guarded_state_vars=guarded_block_vars,
        setter_name=setter_name,
        state_vars=tuple(block_vars))
     */

    private record BlockVars(Set<QualifiedName> condVars, Set<QualifiedName> undefined, int numOut) {

    }

}
