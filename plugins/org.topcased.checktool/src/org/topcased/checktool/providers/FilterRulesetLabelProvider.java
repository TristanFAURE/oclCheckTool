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

import org.eclipse.jface.viewers.LabelProvider;
import org.topcased.checktool.results.Rule;
import org.topcased.checktool.results.RuleFile;
import org.topcased.checktool.results.RuleSet;

public class FilterRulesetLabelProvider extends LabelProvider
{
    public FilterRulesetLabelProvider()
    {
    }

    @Override
    public String getText(Object element)
    {
        if (element instanceof Rule)
        {
            return ((Rule) element).getName();
        }
        else if (element instanceof RuleSet)
        {
            RuleSet set = (RuleSet) element;
            return set.getName() + " v" + set.getVersion().toString();
        }
        else if (element instanceof RuleFile)
        {
            return ((RuleFile) element).getName();
        }
        return super.getText(element);
    }
}
