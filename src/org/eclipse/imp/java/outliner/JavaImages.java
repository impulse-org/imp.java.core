/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.outliner;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class JavaImages {
	public static final String IMAGE_ROOT= "icons";

	public static ImageDescriptor OUTLINE_ITEM_DESC= AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.safari.java.core", IMAGE_ROOT + "/outline_item.gif");

	public static Image OUTLINE_ITEM_IMAGE= OUTLINE_ITEM_DESC.createImage();
}
