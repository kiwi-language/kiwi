package tech.metavm.transpile.ir;

import java.util.List;

public interface SwitchRuleOutcome {

    IRType getType();

    List<Statement>  getStatements();

}
