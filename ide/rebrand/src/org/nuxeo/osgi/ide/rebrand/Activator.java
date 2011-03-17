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

package org.nuxeo.osgi.ide.rebrand;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * @author matic
 * 
 */
public class Activator implements BundleActivator {

    public static final Log log = LogFactory.getLog(Activator.class);

    protected BundleContext context;

    protected BundleRebrander rebrander;

    protected String selfname;

    @Override
    public void start(BundleContext bc) throws Exception {
        context = bc;
        rebrander = new BundleRebrander();
        selfname = context.getBundle().getSymbolicName();
        rebrand();
    }

    protected void rebrand() {
        // rebrand and install new bundles
        List<Bundle> l = new ArrayList<Bundle>();
        for (Bundle b : context.getBundles()) {
            String sn = b.getSymbolicName();
            if (selfname.equals(sn)) {
                continue;
            }
            if (rebrander.relocator.canRelocateClass(sn)) {
                rebrand(b); 
                l.add(b);
            }
        }
        // uninstall rebranded bundles
        for (Bundle b : l) {
            String fh = (String)b.getHeaders().get("Bundle-FragmentHost");
            if (fh == null) {
                try {
                    b.uninstall();
                } catch (BundleException e) {
                    log.error("Cannot uninstall ".concat(b.getSymbolicName()), e);
                }
            }
        }
    }

    protected void rebrand(Bundle b) {
        InputStream is;
        String sn = b.getSymbolicName();
        try {
            is = rebrander.rebrandBundle(context, b, new File("/tmp/pool"));
        } catch (Exception e) {
            log.error("Cannot rebrand " + sn, e);
            return;
        }
        try {
            context.installBundle("rebranded:".concat(sn), is);
        } catch (BundleException e) {
            log.error("Cannot install rebranded for ".concat(sn));
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        rebrander = null;
        context = null;
        selfname = null;
    }

}
