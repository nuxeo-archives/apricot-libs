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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingAnnotationAdapter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.RemappingFieldAdapter;
import org.objectweb.asm.commons.RemappingMethodAdapter;

/**
 * @author matic
 *
 */
public class RebrandClassAdapter extends RemappingClassAdapter {

     private class MethodRemapVisitor
        extends RemappingMethodAdapter
    {
        public MethodRemapVisitor( int access, String desc, MethodVisitor mv,  Remapper remapper)
        {
            super( access, desc, mv, remapper );
        }

        public AnnotationVisitor visitAnnotation( String desc, boolean visible )
        {
            // The original source from asm:3.2 does not have the call to remapper.mapDesc()
            AnnotationVisitor av = mv.visitAnnotation( remapper.mapDesc( desc ), visible );
            return av == null ? av : new RemappingAnnotationAdapter( av, remapper );
        }
    }

    private  class FieldRemapVisitor
        extends RemappingFieldAdapter
    {

        private final FieldVisitor fv;

        public FieldRemapVisitor( FieldVisitor fv, Remapper remapper )
        {
            super( fv, remapper );
            this.fv = fv;
        }

        public AnnotationVisitor visitAnnotation( String desc, boolean visible )
        {
            // The original source from asm:3.2 does not have the call to remapper.mapDesc()
            AnnotationVisitor av = fv.visitAnnotation( remapper.mapDesc( desc ), visible );
            return av == null ? null : new RemappingAnnotationAdapter( av, remapper );
        }
    }

    public RebrandClassAdapter(ClassVisitor cv, Remapper remapper) {
        super(cv, remapper);
    }
    
    @Override
    protected FieldVisitor createRemappingFieldAdapter(FieldVisitor fv) {
        return new FieldRemapVisitor(fv, remapper);
    }
    
    @Override
    protected MethodVisitor createRemappingMethodAdapter(int access,
            String desc, MethodVisitor mv) {
        return new MethodRemapVisitor(access, desc, mv, remapper);
    }
   
}
