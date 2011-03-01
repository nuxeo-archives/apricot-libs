/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.spi.inject;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * The context to obtain {@link Injectable} instances.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface InjectableProviderContext {
    /**
     * Check if one or more injectable provider is registered to process an
     * anotation and a context type for a given scope.
     * 
     * @param ac the annotation class.
     * @param cc the context type.
     * @param s the scope.
     * @return true if one or more injectable provider is registered,
     *         otherwise false.
     */
    boolean isInjectableProviderRegistered(Class<? extends Annotation> ac,
            Class<?> cc,
            ComponentScope s);

    /**
     * Get an injectable.
     * 
     * @param <A> the type of the annotation.
     * @param <C> the the context type. Types of the {@link java.lang.reflect.Type} and 
     *        {@link com.sun.jersey.api.model.Parameter} are the only types that
     *        are supported.
     * @param ac the annotation class.
     * @param ic the injectable context.
     * @param a the annotation instance.
     * @param c the context type.
     * @param s the scope.
     * @return the injectable, otherwise null if an injectable could 
     *         not be found.
     */
    <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            ComponentContext ic,
            A a,
            C c,
            ComponentScope s);
    
    /**
     * Get an injectable.
     * 
     * @param <A> the type of the annotation.
     * @param <C> the the context type. Types of the {@link java.lang.reflect.Type} and 
     *        {@link com.sun.jersey.api.model.Parameter} are the only types that
     *        are supported.
     * @param ac the annotation class.
     * @param ic the injectable context.
     * @param a the annotation instance.
     * @param c the context type.
     * @param ls the list of scope, ordered by preference.
     * @return the injectable, otherwise null if an injectable could 
     *         not be found.
     */
    <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            ComponentContext ic,
            A a,
            C c,
            List<ComponentScope> ls);

    public static final class InjectableScopePair {
        public final Injectable i;
        public final ComponentScope cs;

        public InjectableScopePair(Injectable i, ComponentScope cs) {
            this.i = i;
            this.cs = cs;
        }
    }
    
    /**
     * Get an injectable.
     *
     * @param <A> the type of the annotation.
     * @param <C> the the context type. Types of the {@link java.lang.reflect.Type} and
     *        {@link com.sun.jersey.api.model.Parameter} are the only types that
     *        are supported.
     * @param ac the annotation class.
     * @param ic the injectable context.
     * @param a the annotation instance.
     * @param c the context type.
     * @param ls the list of scope, ordered by preference.
     * @return the injectable and scope, otherwise null if an injectable could
     *         not be found.
     */
    <A extends Annotation, C> InjectableScopePair getInjectableWithScope(
            Class<? extends Annotation> ac,
            ComponentContext ic,
            A a,
            C c,
            List<ComponentScope> ls);
}