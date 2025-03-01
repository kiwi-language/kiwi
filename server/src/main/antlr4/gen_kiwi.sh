TARGET=/Users/leen/workspace/object/kcompiler/src/main/java/org/metavm/compiler/antlr
PKG=org.metavm.compiler.antlr
FILES=(KiwiLexer.g4 KiwiParser.g4)
antlr4 -v 4.13.2 ${FILES[*]}
antlr4 -v 4.13.2 -package $PKG -o $TARGET -visitor ${FILES[*]}
#antlr4 -package $PKG -visitor ${FILES[*]}
#FRONT_TARGET=/Users/leen/workspace/front/src/expression/antlr
#antlr4 -o $FRONT_TARGET -Dlanguage=TypeScript ${FILES[*]}
