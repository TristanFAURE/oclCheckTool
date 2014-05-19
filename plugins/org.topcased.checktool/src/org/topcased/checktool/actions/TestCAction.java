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
package org.topcased.checktool.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.topcased.checktool.utils.CUtils;
import org.topcased.checktool.utils.ICheckToolConstants;

public class TestCAction implements IWorkbenchWindowActionDelegate
{

    private IWorkbenchWindow window;
    
    public void run(IAction action)
    {
        //retrieve all the files. This will be handled differently later on
        List<String> uriStrings = getFiles();
        for(String uriString : uriStrings) {
            //parse the file using CDT
//            CUtils.getInstance().parse(uriString);
        }
    }

    private List<String> getFiles()
    {
//        String uriString = "/cFilesBis/MmdlConfigurationTable_10.c";
        String uriString = "/cFilesBis/Test.c";
        // Find and package the file within our metamodel
//        FileDialog dialog = new FileDialog(window.getShell());
//        dialog.setText(ICheckToolConstants.CFILE_TEXT);
//        dialog.setFilterPath(Platform.getLocation().toString());
//        String uriString = dialog.open();
        return Collections.singletonList(uriString);
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
    }

    public void dispose()
    {
    }

    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }

}
