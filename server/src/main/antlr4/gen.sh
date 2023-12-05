TARGET=/Users/leen/workspace/object/src/main/java/tech/metavm/expression/antlr
PKG=tech.metavm.expression.antlr
FILES=(MetaVMLexer.g4 MetaVMParser.g4)
antlr4 -package $PKG -o $TARGET ${FILES[*]}
FRONT_TARGET=/Users/leen/workspace/front/src/expression/antlr
antlr4 -o $FRONT_TARGET -Dlanguage=TypeScript ${FILES[*]}