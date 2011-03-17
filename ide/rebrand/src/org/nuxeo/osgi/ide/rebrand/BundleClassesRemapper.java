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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.commons.Remapper;

public class BundleClassesRemapper
    extends Remapper
{

    protected final BrandRelocator relocator;
     
    private final Pattern classPattern = Pattern.compile( "(\\[*)?L(.+);" );

    public BundleClassesRemapper(BrandRelocator relocator )
    {
        this.relocator = relocator;
    }

    public Object mapValue( Object object )
    {
        if ( object instanceof String )
        {
            String name = matchClassName((String)object);

            Matcher m = classPattern.matcher( name );
            if ( m.matches() )
            {
                name = m.group( 2 );
            }

            if (relocator.canRelocateClass(name)) {
                return relocator.relocateClass(name);
            }
            if (relocator.canRelocatePath(name)) {
                return relocator.relocatePath(name);
            }
            return name;
           }

        return super.mapValue( object );
    }

    public String matchClassName(String name) {
        Matcher m = classPattern.matcher( name );
        if ( m.matches() )
        {
            return m.group( 2 );
        }
        return name;
    }

    public String map( String name )
    {
            name = matchClassName(name);
            if (relocator.canRelocatePath(name)) {
                return relocator.relocatePath(name);
            }
            if (relocator.canRelocateClass(name)) {
                return relocator.relocateClass(name);
            }
            return name;
    }

}
