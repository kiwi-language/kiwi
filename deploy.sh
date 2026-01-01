rm -rf $HOME/.kiwi
unzip -d $HOME dist/target/kiwi.zip
mv $HOME/kiwi $HOME/.kiwi
cp -f /etc/kiwi/kiwi.yml $HOME/.kiwi/conf
kiwi-server
