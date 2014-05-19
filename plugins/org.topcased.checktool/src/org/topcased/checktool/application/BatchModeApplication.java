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
package org.topcased.checktool.application;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.topcased.checktool.Log;
import org.topcased.checktool.utils.BatchUtils;

public class BatchModeApplication implements IApplication
{

    private static final String PROVIDER_WORKSPACE = "provider";

    private static final String RULESETS_PATH = "rulesets";

    private static final String INHIBITOR = "inhibitor";

    private static final String XSD_PATH = "xsdpath";

    private static final String C_FILES = "cfiles";

    private static final String INCLUDES = "includes";

    private static final String XML_MODELS = "xmlmodels";

    private static final String OUTPUT_PATH = "output";

    public Object start(IApplicationContext context) throws Exception
    {
        Display.getDefault();
        PlatformUI.isWorkbenchRunning();
        String[] arguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        ArgOpt[] opts = new ArgOpt[] {
                new ArgOpt(PROVIDER_WORKSPACE, ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "pw",
                        "provider-given workspace, containing all rulesets and the XSD constraint. This parameter is mandatory"),
                new ArgOpt(RULESETS_PATH, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "rsets",
                        "workspace-relative paths, pointing to the ruleset files that need to be checked. This parameter is optional"),
                new ArgOpt(INHIBITOR, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "inhib", "absolute path, pointing to the inhibitor file. This parameter is optional"),
                new ArgOpt(XSD_PATH, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "xsd", "workspace-relative path, pointing to the main XSD constraint. This parameter is optional"),
                new ArgOpt(C_FILES, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "cfiles", "C Files. This parameter is optional"),
                new ArgOpt(INCLUDES, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "incl", "all header files used by the C files. This parameter is optional"),
                new ArgOpt(XML_MODELS, ArgOpt.OPTIONAL_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "xml", "Direct path to any xml models. This parameter is optional"),
                new ArgOpt(OUTPUT_PATH, ArgOpt.REQUIRED_ARGUMENT, ArgOpt.REQUIRED_ARGUMENT_VALUE, "o", "Path to the final output file. This parameter is mandatory"),

        };
        GetOpt opt = new GetOpt();
        HashMap<String, String> result = opt.getArguments(opts, arguments);

        // Provider
        String providerWorkspace = result.get(PROVIDER_WORKSPACE);
        // rulesets
        String rawRulesets = result.get(RULESETS_PATH);
        List<String> ruleSets = null;
        if (rawRulesets != null)
        {
            ruleSets = Arrays.asList(rawRulesets.split(";"));
        }
        // inhibitor
        String inhibitor = result.get(INHIBITOR);
        // XSD constraint
        String xsdPath = result.get(XSD_PATH);
        // C Files
        String rawCFiles = result.get(C_FILES);
        List<String> cFiles = null;
        if (rawCFiles != null)
        {
            cFiles = Arrays.asList(rawCFiles.split(";"));
        }
        // Includes
        String rawIncludes = result.get(INCLUDES);
        List<String> includes = null;
        if (rawCFiles != null)
        {
            includes = Arrays.asList(rawIncludes.split(";"));
        }
        // XML models
        String rawXmlModels = result.get(XML_MODELS);
        List<String> xmlModels = null;
        if (rawXmlModels != null)
        {
            xmlModels = Arrays.asList(rawXmlModels.split(";"));
        }
        // Output path
        String outputPath = result.get(OUTPUT_PATH);

        if (xmlModels == null && cFiles == null)
        {
            Log.errorLog("Both xmlmodels and cdirectories arguments are either empty or cannot be read");
        }
        else if (xsdPath == null && ruleSets == null)
        {
            Log.errorLog("Both the xsdpath and ruleset arguments are either empty or cannot be read");
        }
        else
        {
            Log.infoLog("Started checking process");
            BatchUtils.instance.checkModelBatch(providerWorkspace, ruleSets, xsdPath, inhibitor, cFiles, includes, xmlModels, outputPath);
        }
        Log.infoLog("Done");
        return null;
    }

    public void stop()
    {
        BatchUtils.instance.destroyLinkProjects();
    }

}
