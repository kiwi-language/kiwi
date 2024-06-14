TARGET=/Users/leen/workspace/object/model/src/main/java/org/metavm/asm/antlr
PKG=org.metavm.asm.antlr
FILES=(AssemblyLexer.g4 AssemblyParser.g4)
antlr4 -package $PKG -o $TARGET -visitor ${FILES[*]}
#antlr4 -package $PKG -visitor ${FILES[*]}
#FRONT_TARGET=/Users/leen/workspace/front/src/expression/antlr
#antlr4 -o $FRONT_TARGET -Dlanguage=TypeScript ${FILES[*]}