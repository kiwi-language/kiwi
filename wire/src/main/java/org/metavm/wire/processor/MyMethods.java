package org.metavm.wire.processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.NoSuchElementException;

public class MyMethods {

    final MyTypes types;

    final Symbol.MethodSymbol readByte;
    final Symbol.MethodSymbol readShort;
    final Symbol.MethodSymbol readInt;
    final Symbol.MethodSymbol readLong;
    final Symbol.MethodSymbol readFloat;
    final Symbol.MethodSymbol readDouble;
    final Symbol.MethodSymbol readBoolean;
    final Symbol.MethodSymbol readChar;
    final Symbol.MethodSymbol readString;
    final Symbol.MethodSymbol readDate;
    final Symbol.MethodSymbol readBytes;
    final Symbol.MethodSymbol readEntity;
    final Symbol.MethodSymbol readNullable;
    final Symbol.MethodSymbol readList;
    final Symbol.MethodSymbol readArray;

    final Symbol.MethodSymbol write;
    final Symbol.MethodSymbol writeByte;
    final Symbol.MethodSymbol writeShort;
    final Symbol.MethodSymbol writeInt;
    final Symbol.MethodSymbol writeLong;
    final Symbol.MethodSymbol writeFloat;
    final Symbol.MethodSymbol writeDouble;
    final Symbol.MethodSymbol writeBoolean;
    final Symbol.MethodSymbol writeChar;
    final Symbol.MethodSymbol writeString;
    final Symbol.MethodSymbol writeDate;
    final Symbol.MethodSymbol writeBytes;
    final Symbol.MethodSymbol writeEntity;
    final Symbol.MethodSymbol writeNullable;
    final Symbol.MethodSymbol writeList;
    final Symbol.MethodSymbol writeArray;

    final Symbol.MethodSymbol visitByte;
    final Symbol.MethodSymbol visitShort;
    final Symbol.MethodSymbol visitInt;
    final Symbol.MethodSymbol visitLong;
    final Symbol.MethodSymbol visitFloat;
    final Symbol.MethodSymbol visitDouble;
    final Symbol.MethodSymbol visitBoolean;
    final Symbol.MethodSymbol visitChar;
    final Symbol.MethodSymbol visitString;
    final Symbol.MethodSymbol visitDate;
    final Symbol.MethodSymbol visitBytes;
    final Symbol.MethodSymbol visitEntity;
    final Symbol.MethodSymbol visitNullable;
    final Symbol.MethodSymbol visitList;
    final Symbol.MethodSymbol visitArray;
    final Symbol.MethodSymbol getInput;

    MyMethods(MyClasses myClasses, Elements elements, MyTypes types, Names names) {
        this.types = types;
        var resolver = new Resolver(elements, names);
        readByte = resolver.resolve(myClasses.wireInput, "readByte");
        readShort = resolver.resolve(myClasses.wireInput, "readShort");
        readInt = resolver.resolve(myClasses.wireInput, "readInt");
        readLong = resolver.resolve(myClasses.wireInput, "readLong");
        readFloat = resolver.resolve(myClasses.wireInput, "readFloat");
        readDouble = resolver.resolve(myClasses.wireInput, "readDouble");
        readBoolean = resolver.resolve(myClasses.wireInput, "readBoolean");
        readChar = resolver.resolve(myClasses.wireInput, "readChar");
        readString = resolver.resolve(myClasses.wireInput, "readString");
        readDate = resolver.resolve(myClasses.wireInput, "readDate");
        readBytes = resolver.resolve(myClasses.wireInput, "readBytes");
        readEntity = resolver.resolve(myClasses.wireInput, "readEntity");
        readNullable = resolver.resolve(myClasses.wireInput, "readNullable");
        readList = resolver.resolve(myClasses.wireInput, "readList");
        readArray = resolver.resolve(myClasses.wireInput, "readArray");

        write = resolver.resolve(myClasses.wireAdapter, "write");
        writeByte = resolver.resolve(myClasses.wireOutput, "writeByte");
        writeShort = resolver.resolve(myClasses.wireOutput, "writeShort");
        writeInt = resolver.resolve(myClasses.wireOutput, "writeInt");
        writeLong = resolver.resolve(myClasses.wireOutput, "writeLong");
        writeFloat = resolver.resolve(myClasses.wireOutput, "writeFloat");
        writeDouble = resolver.resolve(myClasses.wireOutput, "writeDouble");
        writeBoolean = resolver.resolve(myClasses.wireOutput, "writeBoolean");
        writeChar = resolver.resolve(myClasses.wireOutput, "writeChar");
        writeString = resolver.resolve(myClasses.wireOutput, "writeString");
        writeDate = resolver.resolve(myClasses.wireOutput, "writeDate");
        writeBytes = resolver.resolve(myClasses.wireOutput, "writeBytes");
        writeEntity = resolver.resolve(myClasses.wireOutput, "writeEntity");
        writeNullable = resolver.resolve(myClasses.wireOutput, "writeNullable");
        writeList = resolver.resolve(myClasses.wireOutput, "writeList");
        writeArray = resolver.resolve(myClasses.wireOutput, "writeArray");

        visitByte = resolver.resolve(myClasses.wireVisitor, "visitByte");
        visitShort = resolver.resolve(myClasses.wireVisitor, "visitShort");
        visitInt = resolver.resolve(myClasses.wireVisitor, "visitInt");
        visitLong = resolver.resolve(myClasses.wireVisitor, "visitLong");
        visitFloat = resolver.resolve(myClasses.wireVisitor, "visitFloat");
        visitDouble = resolver.resolve(myClasses.wireVisitor, "visitDouble");
        visitBoolean = resolver.resolve(myClasses.wireVisitor, "visitBoolean");
        visitChar = resolver.resolve(myClasses.wireVisitor, "visitChar");
        visitString = resolver.resolve(myClasses.wireVisitor, "visitString");
        visitDate = resolver.resolve(myClasses.wireVisitor, "visitDate");
        visitBytes = resolver.resolve(myClasses.wireVisitor, "visitBytes");
        visitEntity = resolver.resolve(myClasses.wireVisitor, "visitEntity");
        visitNullable = resolver.resolve(myClasses.wireVisitor, "visitNullable");
        visitList = resolver.resolve(myClasses.wireVisitor, "visitList");
        visitArray = resolver.resolve(myClasses.wireVisitor, "visitArray");
        getInput = resolver.resolve(myClasses.wireVisitor, "getInput");
    }

    public boolean methodExists(Symbol.ClassSymbol clazz, Name name, List<? extends TypeMirror> paramTypes) {
        var members = clazz.members().getSymbolsByName(name);
        out: for (Symbol member : members) {
            if (member instanceof Symbol.MethodSymbol method) {
                var params = method.getParameters();
                for (var paramType : paramTypes) {
                    if (params.isEmpty() || !types.isSameType(params.head.type, paramType))
                        continue out;
                    params = params.tail;
                }
                return true;
            }
        }
        return false;
    }

    private record Resolver(Elements elements, Names names) {

        Symbol.MethodSymbol resolve(Symbol.ClassSymbol clazz, String name) {
            var it = clazz.members().getSymbolsByName(names.fromString(name)).iterator();
            if (it.hasNext())
                return (Symbol.MethodSymbol) it.next();
            else
                throw new NoSuchElementException("Cannot find method: " + clazz.getQualifiedName() + "." + name);
        }

    }

}
