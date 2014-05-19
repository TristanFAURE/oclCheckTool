/*****************************************************************************
 * Copyright (c) 2014 AtoS.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 
 *****************************************************************************/
package org.topcased.checktool.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.LabelProvider;

public class FileListLabelProvider extends LabelProvider
{
    @Override
    public String getText(Object element)
    {
        if(element instanceof IFile) {
            return ((IFile) element).getFullPath().toString();
        }
        else return "ERROR - invalid element";
    }
}
