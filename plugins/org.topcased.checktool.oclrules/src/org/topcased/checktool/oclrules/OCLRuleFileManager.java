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
package org.topcased.checktool.oclrules;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.resource.Resource;
import org.topcased.checktool.Log;
import org.topcased.checktool.oclrules.oclRules.OCLRuleFile;
import org.topcased.checktool.oclrules.oclRules.OclRulesFactory;
import org.topcased.checktool.results.ResultsFactory;
import org.topcased.checktool.results.RuleFile;
import org.topcased.checktool.results.RuleFileResult;
import org.topcased.checktool.utils.AbstractRuleFileManager;
import org.topcased.ocl.common.OCLResource;


public class OCLRuleFileManager extends AbstractRuleFileManager
{

    @Override
    public RuleFile createRuleFile(IFile iFile)
    {
        return OclRulesFactory.eINSTANCE.createRuleFile(iFile);
    }

    @Override
    public RuleFileResult evaluateRuleFile(RuleFile file, List<Resource> processedResources)
    {
        RuleFileResult rfResult = null;
        if (file instanceof OCLRuleFile)
        {
            // Completing the file will result in the OCL file being parsed for its constraints. Check the fil's
            // validity afterwards
            // Pass the processedResources so they can contribute to the extent map
            ((OCLRuleFile) file).completeFiles(new BasicEList(processedResources));
            if (!file.isIsValid())
            {
                Log.warningLog("Rule file " + file.getName() + " cannot be evaluated and will be ignored. Refer to previous logs for more information");
                return rfResult;
            }

            OCLResource oclResource = ((OCLRuleFile) file).getResource();
            rfResult = ResultsFactory.eINSTANCE.createRuleFileResult();
            rfResult.setRuleFile(file);
            // Evaluating the rules will automatically fill out the given rulefile result
            for (Resource resource : processedResources)
            {
                ChecktoolOCLCheckerEngine.getInstance().evaluateOCLRules(resource, Collections.singletonList(oclResource), false, new NullProgressMonitor(), rfResult);
            }
        }
        return rfResult;

    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        // Do nothing
    }

}
