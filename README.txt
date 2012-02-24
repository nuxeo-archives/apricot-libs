Items marker with * are provided by Nuxeo - the other ones are from eclipse platform or orbit repository

Core Dependencies:
org.eclipse.osgi.services_3.2.100.v20100503.jar
org.eclipse.osgi_3.6.1.R36x_v20100806.jar
ch.qos.logback.classic_0.9.24.v20100831-0715.jar
ch.qos.logback.core_0.9.24.v20100831-0715.jar
ch.qos.logback.slf4j_0.9.24.v20100831-0715.jar
org.slf4j.api_1.6.1.v20100831-0715.jar
org.slf4j.jcl_1.6.1.v20100831-0715.jar
org.slf4j.log4j_1.6.1.v20100831-0715.jar
org.apache.commons.httpclient_3.1.0.v201012070820.jar (needed by sql.storage - TODO make it optional?)
org.apache.commons.beanutils_1.7.0.v200902170505.jar
org.apache.commons.collections_3.2.0.v201005080500.jar
org.apache.commons.io_2.0.1.v201101200200.jar
org.apache.commons.lang_2.4.0.v201005080502.jar
javax.transaction_1.1.1.v201004190952.jar (*exposed by jvm)
org.apache.xerces_2.9.0.v201101211617.jar
javax.xml_1.3.4.v201005080400.jar
org.apache.xml.resolver_1.2.0.v201005080400.jar
org.apache.xml.serializer_2.7.1.v201005080400.jar
*com.sun.xml.relaxng_1.0.0.201103092313.jar
*com.sun.xml.xsom_0.0.20060306.201103092313.jar
*java_cup.runtime_0.10.0.v201005080400.jar
*org.joda.time_1.6.0.201103092313.jar
*org.dom4j_1.6.1.201103092313.jar (required by core.io)
*org.mvel2_2.0.16.201103092313.jar (required by automation)
*groovy-all_1.5.7.jar (optional - required by automation)
*org.apache.geronimo.components.connector_2.1.3.201103092313.jar
*org.apache.geronimo.components.transaction_2.1.3.201103092313.jar
*org.apache.geronimo.specs.geronimo_j2ee_connector_2.0.0.201103092313.jar


============================
H2 Support:
org.apache.lucene.analysis_2.9.1.v201101211721.jar
org.apache.lucene.core_2.9.1.v201101211721.jar
org.apache.lucene_2.9.1.v201101211721.jar
org.h2_1.1.117.v20091003-1000.jar

============================
Web (JAX-RS) Support:
org.eclipse.equinox.http.jetty_2.0.0.v20100503.jar
org.eclipse.equinox.http.servlet_1.1.0.v20100503.jar
org.mortbay.jetty.server_6.1.23.v201012071420.jar
org.mortbay.jetty.util_6.1.23.v201012071420.jar
javax.ws.rs_1.1.1.v20101004-1200.jar
javax.servlet_2.5.0.v201012071420.jar
javax.activation_1.1.0.v201005080500.jar (used by javax.mail)
javax.mail_1.4.0.v201005080615.jar
*com.sun.jersey.core_1.1.5.201103092313.jar
*com.sun.jersey.server_1.1.5.201103092313.jar
*org.freemarker_2.3.16.201103092313.jar
*net.sf.ezmorph_1.0.4.201103092313.jar
*net.sf.json_2.2.1.201103092313.jar

============================
CMIS Support:
*org.apache.commons.codec_1.4.0.jar
*antlr_2.7.7.jar
*org.antlr.runtime_3.1.3.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-client-api_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-client-bindings_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-client-impl_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-commons-api_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-commons-impl_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-server-bindings_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-server-support_0.2.0.incubating.jar

Optional features: (not needed to be declared as dependencies at eclipse)
===================
1. Management extensions:
*org.javasimon_2.3.0.201103092313.jar
*com.thoughtworks.xstream_1.3.1.201103092313.jar
2. Optional libs:
*jaxws-dev_2.2.0.jar (optional and must not be used at runtime - only needed when developing chemsitry bridge in PDE)
*org.wikimodel.wem_2.0.2.201103092313.jar (for rendering)
javax.annotation_1.0.0.v20101115-0725.jar (optional used by jersey.server)
javax.el_2.1.0.jar (optional - for freemarker)
org.apache.commons.jexl_1.1.0.jar (for runtime)
org.jdom_1.1.1.jar (can be added for xstream and freemarker support)
javax.persistence_1.0.0.jar (used by jersey.server)
org.apache.oro_2.0.8.jar (used by json)
org.apache.xalan_2.7.1.jar (used by freemarker)

Not used?
=========================
javax.xml.stream_1.0.1.v201004272200.jar (is this needed by someone?)
=========================


==============================
*org.nuxeo.runtime 5.4.1
*org.nuxeo.runtime.jtajca 5.4.1
*org.nuxeo.common 5.4.1
...
	Apache 2 license.

*com.sun.xml.relaxng_1.0.0.jar
	BSD like license. See http://xsom.java.net/
*com.sun.xml.xsom_0.0.20060306.jar
	CDDL license. See http://xsom.java.net/
*org.joda.time_1.6.0.jar
	Apache 2 license. See http://joda-time.sourceforge.net/
*org.dom4j_1.6.1.jar
	BSD style license. See http://dom4j.sourceforge.net/license.html
*org.mvel2_2.0.16.jar
	Apache 2.0 License. See http://mvel.codehaus.org/
*groovy-all_1.5.7.jar
	Apache 2 license. See http://groovy.codehaus.org/
*org.apache.geronimo.components.connector_2.1.3.jar
*org.apache.geronimo.components.transaction_2.1.3.jar
*org.apache.geronimo.specs.geronimo_j2ee_connector_2.0.0.jar
	Apache 2 license. See http://geronimo.apache.org/
*com.sun.jersey.core_1.1.5.jar
*com.sun.jersey.server_1.1.5.jar
	CDDL license. See http://jersey.java.net/
*org.freemarker_2.3.16.jar
	BSD-style license. See http://freemarker.sourceforge.net/
*net.sf.ezmorph_1.0.4.jar
	Apache 2 license. See http://ezmorph.sourceforge.net/
*net.sf.json_2.2.1.jar
	Apache 2 license. See http://json-lib.sourceforge.net/
*org.apache.commons.codec_1.4.0.jar
	Apache 2 license. See http://commons.apache.org/codec/
*antlr_2.7.7.jar
*org.antlr.runtime_3.1.3.jar
	BSD like license. See http://www.antlr.org/license.html
*org.apache.chemistry.opencmis.chemistry-opencmis-client-api_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-client-bindings_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-client-impl_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-commons-api_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-commons-impl_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-server-bindings_0.2.0.jar
*org.apache.chemistry.opencmis.chemistry-opencmis-server-support_0.2.0.incubating.jar
	Apache 2 license. See http://chemistry.apache.org/

optional:
*org.wikimodel.wem_2.0.2.jar
	Apache 2 license. See http://code.google.com/p/wikimodel/
*com.thoughtworks.xstream_1.3.1.jar
	BSD style license. See http://xstream.codehaus.org/license.html

only for dev:
*jaxws-dev_2.2.0.jar
	CDDL license. See http://jax-ws.java.net/


*org.javasimon_2.3.0.jar
	LGPL license. See http://code.google.com/p/javasimon/
