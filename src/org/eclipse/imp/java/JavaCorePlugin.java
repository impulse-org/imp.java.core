/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java;

import org.eclipse.imp.java.preferences.JavaPreferenceCache;
import org.eclipse.imp.java.preferences.JavaPreferenceConstants;
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleContext;

public class JavaCorePlugin extends PluginBase {
    public static final String kPluginID= "org.eclipse.imp.java.core";

    public static final String kLanguageID= "java";

    /**
     * The unique instance of this plugin class
     */
    protected static JavaCorePlugin sPlugin;

    public static JavaCorePlugin getInstance() {
        return sPlugin;
    }

    public JavaCorePlugin() {
        super();
        sPlugin= this;
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);

        // Initialize the Preferences fields with the preference store data.
        IPreferenceStore prefStore= getPreferenceStore();

        JavaPreferenceCache.builderEmitMessages= prefStore.getBoolean(JavaPreferenceConstants.P_EMIT_MESSAGES);

        fEmitInfoMessages= JavaPreferenceCache.builderEmitMessages;
    }

    public String getID() {
        return kPluginID;
    }

    public String getLanguageID() {
        return kLanguageID;
    }
}
