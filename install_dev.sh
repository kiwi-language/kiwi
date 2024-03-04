BASEDIR=$(dirname "$0")
mvn clean install -DskipTests
cd $BASEDIR/compiler
mvn compile assembly:single
#cp $BASEDIR/compiler/target/ $BASEDIR/src/main/java
cp target/compiler-1.0-SNAPSHOT-jar-with-dependencies.jar $HOME/develop/metavm/bin/compiler.jar
cd -

#cd $BASEDIR/assembly/target
#java -jar --enable-preview assembly-1.0-SNAPSHOT.jar