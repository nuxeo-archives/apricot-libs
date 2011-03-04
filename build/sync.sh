#!/bin/sh

GEN_NAME=org.nuxeo.osgi.ide.project.generator                                                                                                               
GEN_DIR=../ide/project-generator                                                                                                                               
JAR=${GEN_DIR}/target/${GEN_NAME}-*.jar                                                                                                                     


#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"

if [ ! -f $JAR ]; then
    echo "Building project generator";
    pushd $GEN_DIR;
    mvn3 install;
    popd;
fi

echo "Synchronizing manifests ..."

java $JAVA_OPTS -cp ${JAR} org.nuxeo.osgi.ide.project.generator.SyncManifest "../" "plugins/pom.xml" "."

echo "Done."
