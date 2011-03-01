#/bin/sh
ECLIPSE_HOME=/usr/local/src/eclipse
rm -f artifacts.jar content.jar
java -jar $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_*.jar -debug -consolelog -nosplash -verbose -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher -metadataRepository file:$PWD -artifactRepository file:$PWD -source $PWD -compress -publishArtifacts
java -jar $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_*.jar -debug -consolelog -nosplash -verbose -application org.eclipse.equinox.p2.publisher.CategoryPublisher -metadataRepository file:$PWD -categoryDefinition file:$PWD/category.xml

