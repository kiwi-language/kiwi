TARGET=/Users/leen/workspace/object/model/src/main/java/org/metavm/expression/antlr
PKG=org.metavm.expression.antlr
FILES=(MetaVMLexer.g4 MetaVMParser.g4)
antlr4 -v 4.13.2 ${FILES[*]}
antlr4 -v 4.13.2 -package $PKG -o $TARGET -visitor ${FILES[*]}
FRONT_TARGET=/Users/leen/workspace/front/src/expression/antlr
antlr4 -o $FRONT_TARGET -Dlanguage=TypeScript ${FILES[*]}
