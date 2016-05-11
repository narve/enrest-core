# rest-take3


Download and unzip app-server: 

    wget http://download.jboss.org/wildfly/10.0.0.Final/wildfly-10.0.0.Final.tar.gz
    tar xvf wildfly*.tar.gz

wildfly-10.0.0.Final/bin/jboss-cli.sh and .../standalone.sh etc will be used. Some useful bash stuff:
    
    export PATH=${PATH}:wildfly-10.0.0.Final/bin
    alias jbcli=./wildfly-10.0.0.Final/bin/jboss-cli.sh


Start the appserver, verify localhost:8080 afterwards. Keep this command running in a separate terminal.  

    standalone.sh

Disable the paranoid security of JBoss: 

    jbcli -c --command="/subsystem=ejb3:write-attribute(name=default-missing-method-permissions-deny-access, stringValue=false)"


Setup a file-based H2 database server: 

    mkdir databases
    export NDB=Eks
    jbcli -c --command="data-source add --name=${NDB}PU --connection-url=jdbc\:h2\:databases/${NDB};AUTO_SERVER=TRUE;USER=sa;PASSWORD=${NDB} --jndi-name=java:/jboss/datasources/${NDB} --driver-name=h2 --user-name=sa --password=${NDB}"
    
Or, alternatively, setup a memory-based H2 database server: 

    jbcli -c --command="data-source remove --name=famcalPU"
    jbcli -c --command="data-source add --name=famcalPU --connection-url=jdbc\:h2\:mem:famcal --jndi-name=java:/jboss/datasources/famcal --driver-name=h2"
    

Build project 

    mvn clean install

Deploy project: 

    jbcli -c --command="deploy target/famcal-server.war --force"
    
    