/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.preferences;

/**
 * Caches the values of various preferences from the preference store
 * for convenience and efficiency.<br>
 * May be removed at some point in lieu of direct access to the preference store.
 */
public class JavaPreferenceCache {
    public static boolean builderEmitMessages;

    private JavaPreferenceCache() {}
}
