TARGET=/Users/leen/workspace/kiwi/model/src/main/java/org/metavm/object/type/antlr
PKG=org.metavm.object.type.antlr
FILES=(TypeLexer.g4 TypeParser.g4)
antlr4 -v 4.13.2 ${FILES[*]}
antlr4 -v 4.13.2 -package $PKG -o $TARGET -visitor ${FILES[*]}
#FRONT_TARGET=/Users/leen/workspace/front/src/type/antlr
#antlr4  -o $FRONT_TARGET -Dlanguage=TypeScript ${FILES[*]}
#antlr4 -package $PKG -visitor ${FILES[*]}
#FRONT_TARGET=/Users/leen/workspace/front/src/expression/antlr
#antlr4 -o $FRONT_TARGET -Dlanguage=TypeScript ${FILES[*]}
