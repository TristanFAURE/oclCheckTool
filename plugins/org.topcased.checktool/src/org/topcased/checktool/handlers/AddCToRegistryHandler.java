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
package org.topcased.checktool.handlers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.internal.impl.MessageImpl;
import org.topcased.checktool.utils.HandlerUtils;

public class AddCToRegistryHandler extends AbstractHandler
{

    /**
     * method to adapt an object
     * 
     * @param o the object to adapt
     * @param aClass the class to adapt to
     * @return null if it can't be adapted, adapted object if there is one
     */
    protected <T> T adapt(Object o, Class<T> aClass)
    {
        T f = null;
        if (aClass.isInstance(o))
        {
            f = (T) o;
        }
        else if (o instanceof IAdaptable)
        {
            IAdaptable adaptable = (IAdaptable) o;
            f = (T) adaptable.getAdapter(aClass);
            if (f == null)
            {
                f = (T) Platform.getAdapterManager().getAdapter(o, aClass);
            }
        }
        return f;
    }

    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        // Get the content of the selection
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        if (currentSelection instanceof IStructuredSelection)
        {
            IStructuredSelection selection = (IStructuredSelection) currentSelection;
            boolean atLeastOneOK = false;
            boolean atLeastOneKO = false;
            StringBuilder nominalBuilder = new StringBuilder("The following C files were processed and added to the registry :\n");
            StringBuilder errorBuilder = new StringBuilder("The following C files failed during processing :\n");
            for (Iterator<Object> i = selection.iterator(); i.hasNext();)
            {
                ResourceSet set = new ResourceSetImpl();
                Object o = i.next();
                IFile f = adapt(o, IFile.class);

                if (f != null)
                {
                    Resource res = HandlerUtils.instance.processCModel(f, set);
                    if (res != null)
                    {
                        atLeastOneOK = true;
                        nominalBuilder.append("-" + f.getName() + "\n");
                    }
                    else
                    {
                        atLeastOneKO = true;
                        errorBuilder.append("-" + f.getName() + "\n");
                    }
                }
                else
                {
                    Collection< ? > collec = adapt(o, Collection.class);
                    if (collec != null)
                    {
                        for (Object o2 : collec)
                        {
                            IFile f2 = adapt(o2, IFile.class);
                            if (f2 != null)
                            {
                                Resource res = HandlerUtils.instance.processCModel(f2, set);
                                if (res != null)
                                {
                                    atLeastOneOK = true;
                                    nominalBuilder.append("-" + f2.getName() + "\n");
                                }
                                else
                                {
                                    atLeastOneKO = true;
                                    errorBuilder.append("-" + f2.getName() + "\n");
                                }
                            }
                        }
                    }
                }
            }

            if (atLeastOneOK || atLeastOneKO)
            {
            	MessageDialog.open(MessageDialog.INFORMATION, Display.getDefault().getActiveShell(), "Information", (atLeastOneOK ? nominalBuilder.toString() : "") + (atLeastOneKO ? errorBuilder.toString() : ""), SWT.NONE);
            }
        }
        return null;
    }

}
