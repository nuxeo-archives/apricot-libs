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
package org.nuxeo.osgi.ide.rebrand.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.osgi.ide.rebrand.BrandRelocator;
import org.nuxeo.osgi.ide.rebrand.BundleRebrander;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;
import org.osgi.framework.Bundle;

/**
 * @author matic
 * 
 */
public class TestCanRebrandBundles {

    protected BrandRelocator relocator;

    protected BundleRebrander rebrander;

    @Before
    public void setupRebrander() {
        relocator = new BrandRelocator("org.nuxeo", "org.eclipse.ecr");
        rebrander = new BundleRebrander(relocator);
    }

    protected void checkBytecode(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ClassReader cr = new ClassReader(is);
        ClassVisitor cv = new TraceClassVisitor(new PrintWriter(bos));
        cr.accept(cv, 0);
        String bytecode = new String(bos.toByteArray());
        assertFalse(relocator.canRelocateClass(bytecode));
        assertFalse(relocator.canRelocatePath(bytecode));
    }

    protected <T> void checkClassRebranding(Class<T> clazz) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(bos);
        try {
            rebrander.rebrandClass(jos, clazz);
        } finally {
            jos.close();
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        JarInputStream jis = new JarInputStream(bis);
        try {
            jis.getNextJarEntry(); // set index
            checkBytecode(jis);
        } finally {
            jis.close();
        }        
    }
    
   protected InputStream getResource(String path) {
        ClassLoader cl = getClass().getClassLoader();
        return cl.getResourceAsStream(path);
    }
    
    @Test  public void rebrandAnnotation() throws IOException {
        checkClassRebranding(AnnotationForTest.class);
    }

    @Test public void rebrandClassImpl() throws IOException {
        checkClassRebranding(ImplForTest.class);
    }


   @Test public void rebrandSelfBundle() throws IOException, URISyntaxException {
        Bundle b = org.osgi.framework.FrameworkUtil.getBundle(getClass());
        InputStream is = rebrander.rebrandBundle(b.getBundleContext(), b);
        // TOD check content
    }

     @Test
    public void rebrandSelfManifest() throws IOException {
        JarOutputStream jos =  BundleRebrander.createOutputJar();
        String path = "META-INF/MANIFEST.MF";
        InputStream is = getResource(path);
        try {
            rebrander.rebrandManifest(path, jos, new Manifest(is));
        } finally {
            jos.close();
            is.close();
        }
        // TODO check content
    }

    @Test
    public void rebrandSelfContribution() throws IOException {
        JarOutputStream jos =  BundleRebrander.createOutputJar();
        String path = "OSGI-INF/test-contrib.xml";
        InputStream is = getResource(path);
        rebrander.rebrandResource(path, jos, is);
        jos.close();
        // TODO check content
    }


}
