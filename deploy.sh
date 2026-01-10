rm -rf $HOME/.manul
unzip -d $HOME dist/target/manul.zip
mv $HOME/manul $HOME/.manul
cp -f /etc/manul/manul.yml $HOME/.manul/conf
manul-server
