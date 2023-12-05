package tech.metavm.autograph;

import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiVariable;
import tech.metavm.util.NncUtils;

public class BlockAnnotator extends VisitorBase {

    private Block block;

    @Override
    public void visitCodeBlock(PsiCodeBlock block) {
        enterBlock(block);
        super.visitCodeBlock(block);
        exitBlock();
    }

    @Override
    public void visitStatement(PsiStatement statement) {
        statement.putUserData(Keys.CONTAINING_BLOCK, block);
        super.visitStatement(statement);
    }

    @Override
    public void visitVariable(PsiVariable variable) {
        currentBlock().declName(variable.getName());
    }

    private Block currentBlock() {
        return NncUtils.requireNonNull(block);
    }

    private void enterBlock(PsiCodeBlock block) {
        this.block = new Block(this.block, block);
    }

    private void exitBlock() {
        if (block.parent != null) {
            block.parent.declaredNames.addAll(block.declaredNames);
            block = block.parent;
        } else {
            block = null;
        }
    }


}
