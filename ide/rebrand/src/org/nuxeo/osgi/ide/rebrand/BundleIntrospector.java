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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @author matic
 * 
 */
public class BundleIntrospector {

    protected final Bundle bundle;
    
    protected final BundleContext context;
    
    public BundleIntrospector(Bundle b) {
        bundle = b;
        context = bundle.getBundleContext();
    }
    
    public URL getManifest() {
        String mfp = "META-INF/MANIFEST.MF";
        URL u = bundle.getEntry(mfp);
        if (u != null) {
            return u;
        }
        return bundle.getEntry(mfp.toLowerCase());
    }

    public URL[] getResources() {
        Enumeration<URL> e = bundle.findEntries("/", "*", true);
        Set<URL> s = new HashSet<URL>();
        while(e.hasMoreElements()) {
            URL u = e.nextElement();
            String p = u.getPath();
            if ("/META-INF/MANIFEST.MF".equals(p.toUpperCase())) {
                continue;
            }
            s.add(u);
        }
        return s.toArray(new URL[s.size()]);
    }
 
    public Class<?>[] getClasses() {
        ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) context.getService(ref);
        List<Class> classes = new ArrayList<Class>();
        ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
        for (ExportedPackage ePackage : exportedPackages) {
            String packageName = ePackage.getName();
            String packagePath = "/" + packageName.replace('.', '/');
            // find all the class files in current exported package
            Enumeration<URL> clazzes = bundle.findEntries(packagePath, "*.class",
                    false);
            while (clazzes.hasMoreElements()) {
                URL url = (URL) clazzes.nextElement();
                String path = url.getPath();
                int index = path.lastIndexOf("/");
                int endIndex = path.length() - 6;// Strip ".class" substring
                String className = path.substring(index + 1, endIndex);
                String fullClassName = packageName + "." + className;
                try {
                    classes.add(bundle.loadClass(fullClassName));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

}
