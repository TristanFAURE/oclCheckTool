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
package org.topcased.checktool.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.emf.ecore.resource.Resource;

import org.topcased.checktool.results.RuleFile;
import org.topcased.checktool.results.RuleFileResult;

/**
 * @author proland
 */
public abstract class AbstractRuleFileManager implements IExecutableExtension
{

    public abstract RuleFile createRuleFile(IFile iFile);
    
    public abstract RuleFileResult evaluateRuleFile(RuleFile file, List<Resource> processedResources);

}
