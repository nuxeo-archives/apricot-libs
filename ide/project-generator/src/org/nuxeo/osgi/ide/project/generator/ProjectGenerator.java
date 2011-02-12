/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.osgi.ide.project.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ProjectGenerator {

    public static final String PROJECT_NAME_SUFFIX = "-osgi";

    public File pluginRoot;
    
    public File testsRoot;
    
    /**
     * Source project root
     */
    public File src;

    public File osgiRoot;

    public File parentPom;

    public String pathToParentPom; // path in the form ../../..
    public String pathToSrc; // path in the form ../../..

    public PomLoader loader;
    
    public ProjectGenerator(File javaRoot, File osgiRoot, File parentPom, String path) throws Exception {
        this.osgiRoot = osgiRoot.getCanonicalFile();
        this.parentPom = parentPom.getCanonicalFile();
        this.pluginRoot = new File (osgiRoot, "plugins" + File.separator + path);
        this.testsRoot = new File(osgiRoot, "tests" + File.separator + path);
        this.src = new File(javaRoot, path).getCanonicalFile();
        String pathToNuxeo = getDescendingRelativePath(javaRoot, pluginRoot);
        this.pathToSrc = pathToNuxeo+File.separator+path;
        this.pathToParentPom = getDescendingRelativePath(this.parentPom.getParentFile().getCanonicalFile(), pluginRoot)+File.separator+parentPom.getName();
        loader = new PomLoader(new File(src, "pom.xml"));
    }

    public String pathToSrcFile(String ... segments) {
        StringBuilder buf = new StringBuilder(pathToSrc);
        for (String segment : segments) {
            buf.append(File.separator).append(segment);
        }
        return buf.toString();
    }

    public static String getRelativePath(File parent, File file) {
        String p1 = parent.getAbsolutePath();
        String p2 = file.getAbsolutePath();
        if (p1.endsWith(File.separator)) {
            p1 = p1.substring(0, p1.length()-1);
        }
        if (p2.endsWith(File.separator)) {
            p2 = p2.substring(0, p2.length()-1);
        }
        if (!p2.startsWith(p1)) {
            throw new IllegalArgumentException("Invalid path "+file+". Not a sub path of "+parent);
        }
        return p2.substring(p1.length()+1);
    }


    public static String getDescendingRelativePath(File parent, File file) {
        String path = getRelativePath(parent, file);
        String[] ar = StringUtils.split(path, File.separatorChar, false);
        if (ar.length == 0) {
            return ".";
        }
        if (ar.length == 1) {
            return "..";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("..");
        for (int i=1; i<ar.length; i++) {
            buf.append(File.separator).append("..");
        }
        return buf.toString();
    }

    public String getEclipseLinkPrefix() {
        int p = pathToSrc.lastIndexOf("..");
        if (p == -1) {
            throw new IllegalArgumentException("BUG? invalid pathToSrc: "+pathToSrc);
        }
        String prefix = pathToSrc.substring(0, p+2);
        String path = pathToSrc.substring(p+2);
        String[] ar = StringUtils.split(prefix, File.separatorChar, false);
        return "PARENT-"+ar.length+"-PROJECT_LOC"+path+File.separator;
    }

    public File getSourceMainManifest() throws IOException {
        return new File(src, "src"+File.separator+"main"+File.separator+"resources"+File.separator+"META-INF"+File.separator+"MANIFEST.MF").getCanonicalFile();
    }

    public File getSourceTestManifest() throws IOException {
        return new File(src, "src"+File.separator+"test"+File.separator+"resources"+File.separator+"META-INF"+File.separator+"MANIFEST.MF").getCanonicalFile();
    }
    
    public File getPluginManifest() throws IOException {
        return new File(pluginRoot, "META-INF"+File.separator+"MANIFEST.MF").getCanonicalFile();
    }
    
    public File getTestsManifest() throws IOException {
        return new File(testsRoot, "META-INF"+File.separator+"MANIFEST.MF").getCanonicalFile();
    }
    
    public String getSymbolicName(File f) throws Exception {
    	Manifest mf = new Manifest();
    	mf.read(new FileInputStream(f));
    	Attributes a = mf.getMainAttributes();
    	String v = a.getValue("Bundle-SymbolicName");
    	if (v == null) {
    		throw new Exception("No symbolic name for " + f.getCanonicalPath());
    	}
    	return v.split(";")[0];
    }

    public void generate(Map<String,String> parentVars, boolean clean) {
        try {
            if (clean && pluginRoot.isDirectory()) {
                FileUtils.deleteTree(pluginRoot);
            }
            doGenerate(parentVars);
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Failed to generate project: "+pluginRoot);
        }
    }

    public void doGenerate(Map<String,String> parentVars) throws Exception {
        pluginRoot.mkdirs();
        Map<String,String> vars = new HashMap<String, String>(parentVars);
        String artifactId = loader.getArtifactId() + PROJECT_NAME_SUFFIX;
        String version = loader.getVersion();
        if (version.isEmpty()) {
            version = parentVars.get("parentVersion");
        }
        String groupId = loader.getGroupId();
        if (groupId.isEmpty()) {
            groupId = loader.getParentGroupId();
        }
        vars.put("artifactId", artifactId);
        vars.put("groupId", groupId);
        vars.put("version", version);
        vars.put("name", loader.getProjectName());
        vars.put("description", loader.getProjectDescription());
        vars.put("pathToParentPom", pathToParentPom);
        vars.put("pathToSrc", pathToSrc);

        vars.put("projectName", artifactId);
        try {
        	vars.put("projectName", getSymbolicName(getSourceMainManifest()));
        } catch (IOException e) {
        	;
        }
        if (version.endsWith("-SNAPSHOT")) {
            version = version.substring(0, version.length()-"-SNAPSHOT".length()).concat(".qualifier");
        }
        //TODO bundle version is not a template ...
        // we must copy MANIFEST.MF from now and replace the original Bundle-Version
        vars.put("bundleVersion", version);

        vars.put("javaPath", pathToSrcFile("src", "main", "java"));
        vars.put("resourcesPath", pathToSrcFile("src", "main", "resources"));

        String prefix = getEclipseLinkPrefix();
        vars.put("javaLink", prefix+"src"+File.separator+"main"+File.separator+"java");
        vars.put("resourcesLink", prefix+"src"+File.separator+"main"+File.separator+"resources");
        // all vars are setup -> start copying and processing templates

        copyTemplate(pluginRoot, "templates/plugin/.project", ".project", vars);
        copyTemplate(pluginRoot, "templates/plugin/.classpath", ".classpath", vars);
        copyTemplate(pluginRoot, "templates/plugin/build.properties", "build.properties", vars);
        copyTemplate(pluginRoot, "templates/plugin/pom.xml", "pom.xml", vars);
        copyManifest(getSourceMainManifest(), getPluginManifest(), version);
        
        copyTemplate(testsRoot, "templates/tests/.project", ".project", vars);
        copyTemplate(testsRoot, "templates/tests/.classpath", ".classpath", vars);
        copyTemplate(testsRoot, "templates/tests/build.properties", "build.properties", vars);
        copyTemplate(testsRoot, "templates/tests/pom.xml", "pom.xml", vars);
        copyManifest(getSourceTestManifest(), getTestsManifest(), version);
    }

    protected static void copyTemplate(File dir, String path, String filePath, Map<String,String> vars) throws IOException {
        URL url = ProjectGenerator.class.getResource(path);
        if (url == null) {
            throw new Error("Resource not found: "+path);
        }
        copyTemplate(url, new File(dir, filePath), vars);
    }

    protected void copyManifest(File src, File mf, String v) throws IOException {
        mf.getParentFile().mkdirs();
        if (!src.exists()) {
        	return;
        }
        String content = FileUtils.readFile(src);
        FileUtils.writeFile(mf, content.replace("0.0.0.SNAPSHOT", v));
    }

    protected static void copyTemplate(URL url, File toFile, Map<String,String> vars) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("Null url");
        }
        toFile.getParentFile().mkdirs();
        InputStream in = url.openStream();
        try {
            String content = FileUtils.read(in);
            content = StringUtils.expandVars(content, vars);
            FileUtils.writeFile(toFile, content);
        } finally {
            in.close();
        }
    }

    public static void generate(File javaRoot, File pom, File osgiRoot,  boolean clean) throws Exception {
        javaRoot = javaRoot.getCanonicalFile();
        pom = pom.getCanonicalFile();
        osgiRoot = osgiRoot.getCanonicalFile();
        System.out.println("====== Generate PDE projects ======");
        System.out.println("Nuxeo Java Root: "+javaRoot);
        System.out.println("Parent POM: "+pom);
        System.out.println("Nuxeo OSGi Root: "+osgiRoot);
        System.out.println("===================================");
        PomLoader loader = new PomLoader(pom);
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("parentVersion", loader.getVersion());
        vars.put("parentArtifactId", loader.getArtifactId());
        vars.put("parentGroupId", loader.getGroupId());
        for (String pluginPath : loader.getModulesPath()) {
            System.out.println("Generating " + pluginPath);
            new ProjectGenerator(javaRoot, osgiRoot, pom, pluginPath).generate(vars, clean);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3 && args.length > 4) {
            System.err.println("Usage: ProjectGenerator [-clean] nuxeoRoot pom osgiRoot");
            return;
        }
        boolean clean = false;
        String nuxeoRoot = null;
        String osgiRoot = null;
        String pom = null;
        if (args.length == 4) {
            if (!"-clean".equals(args[0])) {
                System.err.println("Usage: ProjectGenerator [-clean] nuxeoRoot pom");
                return;
            }
            clean = true;
            nuxeoRoot = args[1];
            osgiRoot = args[2];
            pom = args[3];
        } else {
            nuxeoRoot = args[0];
            osgiRoot = args[1];
            pom = args[2];
        }

        generate(new File(nuxeoRoot), new File(pom), new File(osgiRoot), clean);
    }

}
