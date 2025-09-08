kiwi-server stop
rm -rf $HOME/develop/kiwi
unzip -d $HOME/develop dist/target/kiwi.zip
cp -f /etc/kiwi/kiwi.yml $HOME/develop/kiwi/config
kiwi-server start
