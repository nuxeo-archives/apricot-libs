/*******************************************************************************
 * Copyright 2011 matic
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     matic
 */
package org.nuxeo.osgi.ide.rebrand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author matic
 * 
 */
public class BundleRebrander {

    private static final Log log = LogFactory.getLog(BundleRebrander.class);

    protected final BrandRelocator relocator;

    protected final BundleClassesRemapper remapper;

    public BundleRebrander() {
        this(new BrandRelocator("org.nuxeo", "org.eclipse.ecr"));
    }

    public BundleRebrander(BrandRelocator r) {
        this.relocator = r;
        this.remapper = new BundleClassesRemapper(relocator);
    }

    public void rebrandJar(JarOutputStream jos, JarInputStream jis)
            throws IOException {
        rebrandManifest("META-INF/MANIFEST.MF", jos, jis.getManifest());
        JarEntry je = jis.getNextJarEntry();
        while (je != null) {
            String path = je.getName();
            if (path.endsWith(".class")) {
                rebrandClass(path, jos, jis);
            } else {
                rebrandResource(path, jos, jis);
            }
            jis.closeEntry();
            je = jis.getNextJarEntry();
        }
    }

    public void rebrandResource(String path, JarOutputStream jos, InputStream is)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = new byte[1024 * 1024];
        int read;
        while ((read = is.read(bytes)) > 0) {
            sb.append(new String(bytes, 0, read));
        }
        String txt = relocator.relocateClass(sb.toString());
        path = relocator.relocatePath(path);
        jos.putNextEntry(new JarEntry(path));
        try {
            jos.write(txt.getBytes());
        } finally {
            jos.closeEntry();
        }
        log.debug("Rebranded resource -> ".concat(path));
    }

    public void rebrandManifest(String path, JarOutputStream jos,
            Manifest source) throws IOException {
        Manifest target = new Manifest();
        Attributes sourceAttrs = source.getMainAttributes();
        Attributes targetAttrs = target.getMainAttributes();
        for (Object name : sourceAttrs.keySet()) {
            String directive = name.toString();
            String value = sourceAttrs.getValue(directive);
            value = relocator.relocateClass(value);
            value = relocator.relocatePath(value);
            targetAttrs.put(name, value);
        }
        jos.putNextEntry(new JarEntry(path));
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            target.write(bos);
            jos.write(bos.toByteArray());
        } finally {
            jos.closeEntry();
        }
        log.debug("Rebranded manifest -> ".concat(path));
    }

    public void rebrandClass(JarOutputStream jos, Class<?> clazz) throws IOException {
        Bundle b = FrameworkUtil.getBundle(clazz);
        String name = clazz.getSimpleName();
        String path = clazz.getName().replace('.', '/').concat(".class");
         Enumeration<URL> e ;
        try {
            e = b.findEntries("/", name.concat(".class"), true);
        } catch (Throwable t) {
            throw new Error(t);
        }
        InputStream is = null;
        while (e.hasMoreElements()) {
            URL u = e.nextElement();
            if (u.getPath().endsWith(path)) {
                is = u.openStream();
                break;
            }
        }
        try {
            rebrandClass(path, jos, is);
        } finally {
            is.close();
        }
    }
    
    public void rebrandClass(String path, JarOutputStream jos, InputStream is)
            throws IOException {
        ClassReader cr = new ClassReader(is);

        ClassWriter cw = new ClassWriter(cr, 0);

        ClassVisitor cv = new RebrandClassAdapter(cw, remapper);

        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        byte[] renamedClass = cw.toByteArray();

        // Need to take the .class off for remapping evaluation
        path = path.substring(0, path.indexOf('.'));
        path = remapper.map(path);
        path = path.concat(".class");

        jos.putNextEntry(new JarEntry(path));
        try {
            jos.write(renamedClass);
        } finally {
            jos.closeEntry();
        }
        log.debug("Rebranded class -> ".concat(path));
    }

    public static JarOutputStream createOutputJar() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(bos);
        return jos;
    }

    public static void main(String[] args) throws FileNotFoundException,
            IOException {
        File f = new File(args[0]);
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "wrong arguments count, should be 3 : jarfile inprefix outprefix");
        }
        JarInputStream jis = new JarInputStream(new FileInputStream(f));
        JarOutputStream jos = createOutputJar();
        new BundleRebrander(new BrandRelocator(args[1], args[2])).rebrandJar(
                jos, jis);
    }


    protected static JarInputStream buildBundleJar(Bundle b) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(bos);
        BundleIntrospector bi = new BundleIntrospector(b);
        addBundleFile(jos, new JarEntry("META-INF/MANIFEST.MF"),
                bi.getManifest().openStream());
        for (URL u : bi.getResources()) {
            String p = u.getPath();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            JarEntry je = new JarEntry(p);
            if (p.endsWith("/")) {
                continue;
            }
            InputStream is = u.openStream();
            try {
                addBundleFile(jos, je, is);
            } finally {
                is.close();
            }
        }
        jos.close();
        return new JarInputStream(new ByteArrayInputStream(bos.toByteArray()));
    }

    protected static void addBundleDir(JarOutputStream jos, JarEntry je)
            throws IOException {
        jos.putNextEntry(je);
        jos.closeEntry();
    }

    protected static void addBundleFile(JarOutputStream jos, JarEntry je,
            InputStream is) throws IOException {
        jos.putNextEntry(je);
        try {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                jos.write(buffer, 0, read);
            }
        } finally {
            jos.closeEntry();
        }
    }

    public InputStream rebrandBundle(BundleContext bc, Bundle b) throws IOException,
            URISyntaxException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rebrandBundle(bc, b, bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    public void rebrandBundle(BundleContext bc, Bundle b, OutputStream os)
            throws IOException {
        JarOutputStream jos = new JarOutputStream(os);
        JarInputStream jis = buildBundleJar(b);
        try {
            rebrandJar(jos, jis);
        } finally {
            jos.close();
            jis.close();
        }
        log.info("Rebranded bundle ->".concat(b.getSymbolicName()));
    }

    public InputStream rebrandBundle(BundleContext bc, Bundle b, File pool)
            throws IOException, URISyntaxException {
         File f = new File(pool, b.getSymbolicName()
         .concat(".").concat(b.getVersion().toString())+ "-rebranded.jar");
         f.delete();
         f.createNewFile();
         FileOutputStream fos = new FileOutputStream(f);
         rebrandBundle(bc, b, fos);
         return new FileInputStream(f);
    }


}
