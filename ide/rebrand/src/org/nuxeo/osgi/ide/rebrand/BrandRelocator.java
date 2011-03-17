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


/**
 * @author matic
 *
 */
public class BrandRelocator {
    
    public BrandRelocator(String source, String target) {
        classSource = source;
        classTarget = target;
        pathSource = classSource.replace('.','/');
        pathTarget = classTarget.replace('.','/');
    }
        
    protected String pathSource;
    
    protected String pathTarget;

    protected String classSource;
    
    protected String classTarget;
    

    public boolean canRelocatePath( String path ) {
        return path.startsWith(pathSource);
    }


    public String relocatePath( String path ) {
        path = path.replace(pathSource, pathTarget);
        path = relocateClass(path);
        return path;
    }


     public boolean canRelocateClass( String clazz ) {
        return clazz.startsWith(classSource);
    }


   public String relocateClass( String clazz ) {
        clazz = clazz.replace(classSource, classTarget);
        return clazz;
    }
}
