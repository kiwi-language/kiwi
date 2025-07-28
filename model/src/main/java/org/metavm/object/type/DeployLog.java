package org.metavm.object.type;

import java.util.ArrayList;
import java.util.List;

public class DeployLog {

    private int errorCount;
    private  final List<DeployDiag> diags = new ArrayList<>();

    public void addDiag(DeployDiag diag) {
        diags.add(diag);
        if (diag instanceof DeployError)
            errorCount++;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public boolean isError() {
        return errorCount > 0;
    }

    public String getMessage() {
        StringBuilder message = new StringBuilder();
        for (DeployDiag diag : diags) {
            if (!message.isEmpty()) {
                message.append("\n");
            }
            message.append(diag.message());
        }
        return message.toString();
    }
}
