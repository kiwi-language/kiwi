package tech.metavm.transpile;

import static tech.metavm.transpile.TsLexicon.COMMA;
import static tech.metavm.transpile.TsLexicon.IDENTIFIER;

public class LexiconOutputStream {

    private final StringBuilder buffer = new StringBuilder();
    private boolean newLineStarted;
    private TsLexicon lastLexicon;
    private int indent;

    public void writeComma() {
        writeKeyword(COMMA);
    }

    public void writeKeyword(TsLexicon lexicon) {
        writeToken(lexicon, lexicon.text());
    }

    public void writeIdentifier(String identifier) {
        writeToken(IDENTIFIER, identifier);
    }

    public void writeOperatorBefore(String op) {
        writeKeyword(TsLexicon.getByTextExcludingPostfix(op));
    }

    public void writeOperatorAfter(String op) {
        writeKeyword(TsLexicon.getByTextExcludingPrefix(op));
    }

    private boolean shouldWritePrecedingWhiteSpace(TsLexicon lexicon) {
        return !newLineStarted && !lexicon.noPrecedingWhiteSpace() &&
                (lastLexicon == null || !lastLexicon.noSucceedingWhiteSpace());
    }

    private void writeToken(TsLexicon lexicon, String token) {
        if(lexicon == TsLexicon.OPEN_CURLY_BRACE) {
            indent++;
        }
        else if(lexicon == TsLexicon.CLOSING_CURLY_BRACE) {
            indent--;
        }
        if(shouldWritePrecedingWhiteSpace(lexicon)) {
            buffer.append(' ');
        }
        if(newLineStarted) {
            newLineStarted = false;
        }
        if(lexicon.newLineBefore()) {
            writeNewLine();
        }
        buffer.append(token);
        if(lexicon.newLineAfter()) {
            writeNewLine();
        }
        lastLexicon = lexicon;
    }

    public void writeNewLine() {
        buffer.append('\n');
        newLineStarted = true;
        for (int i = 0; i < indent; i++) {
            buffer.append("  ");
        }
    }

    public String toString() {
        return buffer.toString();
    }

}
