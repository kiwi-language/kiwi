kiwi-server stop
rm -rf $HOME/develop/kiwi
unzip -d $HOME/develop dist/target/kiwi.zip
cp -f /etc/kiwi/kiwi.yml $HOME/develop/kiwi/config
kiwi-server start

#BASEDIR=$(dirname "$0")
#mvn clean install -DskipTests
#cd $BASEDIR/kcompiler
#mvn compile assembly:single
#cp $BASEDIR/compiler/target/ $BASEDIR/src/main/java
#cp target/metavm-kcompiler-1.0-SNAPSHOT-jar-with-dependencies.jar $HOME/develop/kiwi/bin/compiler.jar
#cd -

#cd $BASEDIR/assembly/target
#java -jar --enable-preview assembly-1.0-SNAPSHOT.jar
