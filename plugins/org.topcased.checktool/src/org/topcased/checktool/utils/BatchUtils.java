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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.topcased.checktool.Log;
import org.topcased.checktool.results.ResultsFactory;
import org.topcased.checktool.results.ResultsPackage;
import org.topcased.checktool.results.Rule;
import org.topcased.checktool.results.RuleFile;
import org.topcased.checktool.results.RuleSet;


public class BatchUtils
{

    private static final String LINK_PROJECT = "LinkProject";

    private static final String C_LINK_PROJECT = "CLinkProject";

    public static BatchUtils instance = new BatchUtils();

    public void checkModelBatch(String rulesetWorkspacePath, List<String> ruleSetPaths, String xsdPath, String inhibitor, List<String> cModels, List<String> xmlModels, List<String> includes,
            String resultsPath)
    {
        Log.infoLog("Preparing temporary working space");
        IProject linkProject = ResourcesPlugin.getWorkspace().getRoot().getProject(LINK_PROJECT);
        IProject cLinkProject = ResourcesPlugin.getWorkspace().getRoot().getProject(C_LINK_PROJECT);
        initProjects(linkProject, cLinkProject);

        Log.infoLog("Importing rulesets");
        List<RuleSet> rulesets = findAndInitRulesets(new File(rulesetWorkspacePath.trim()), ruleSetPaths, xsdPath, inhibitor, linkProject);
        if (rulesets == null || rulesets.isEmpty())
        {
            return;
        }

        Log.infoLog("Importing model files");
        List<IFile> modelFiles = getModelIFiles(cModels, includes, xmlModels, cLinkProject);
        if (modelFiles == null || modelFiles.isEmpty())
        {
            return;
        }

        Log.infoLog("Checking files");
        try
        {
            HandlerUtils.instance.doCheck(modelFiles, rulesets, resultsPath);
        }
        catch (CoreException e)
        {
            Log.errorLog(e.getMessage());
        }
    }

    protected void initProjects(IProject linkProject, IProject cLinkProject)
    {
        try
        {
            linkProject.delete(true, new NullProgressMonitor());
            cLinkProject.delete(true, new NullProgressMonitor());

            if (!linkProject.exists())
            {
                linkProject.create(new NullProgressMonitor());
            }
            if (!linkProject.isOpen())
            {
                linkProject.open(new NullProgressMonitor());
            }
            if (!cLinkProject.exists())
            {
                IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(cLinkProject.getName());
                IProject project = CCorePlugin.getDefault().createCDTProject(description, cLinkProject, new NullProgressMonitor());

                ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
                ICProjectDescription des = mngr.createProjectDescription(project, false);
                ManagedProject mProj = new ManagedProject(des);
                Configuration cfg = new Configuration(mProj, null, "org.topcased.checktool.cLinkproject", "CheckToolConfigurationName");
                IBuilder bld = cfg.getEditableBuilder();
                bld.setManagedBuildOn(false);
                CConfigurationData data = cfg.getConfigurationData();
                des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
                // Persist the project description
                mngr.setProjectDescription(project, des);

                cLinkProject = project;
            }
            if (!cLinkProject.isOpen())
            {
                cLinkProject.open(new NullProgressMonitor());
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
    }

    protected List<IFile> getModelIFiles(List<String> cModels, List<String> xmlModels, List<String> includes, IProject linkProject)
    {
        List<IFile> modelIFiles = new ArrayList<IFile>();

        getCModels(cModels, linkProject, modelIFiles);

        getIncludes(includes, linkProject);

        getXMLModels(xmlModels, linkProject, modelIFiles);

        return modelIFiles;
    }

    protected void getXMLModels(List<String> xmlModels, IProject linkProject, List<IFile> modelIFiles)
    {
        if (xmlModels != null)
        {
            for (String xmlModel : xmlModels)
            {
                File xmlFile = new File(xmlModel.trim());
                IFile iFile = linkProject.getFile(new Path(xmlFile.getName()));
                try
                {
                    iFile.createLink(new Path(xmlFile.getAbsolutePath()), 0, new NullProgressMonitor());
                    modelIFiles.add(iFile);
                }
                catch (CoreException e)
                {
                    Log.errorLog("Could not import XML models because of error " + e.getMessage());

                }
            }
        }
    }

    protected void getIncludes(List<String> includes, IProject linkProject)
    {
        if (includes != null && !includes.isEmpty())
        {
            try
            {
                // Current implementation groups all include headers into one project-level folder called includes
                // This is liable to change when or if model-specific include directories are added
                IFolder iFolder = linkProject.getFolder("includes");
                if (!iFolder.exists())
                {
                    iFolder.create(0, true, new NullProgressMonitor());
                }
                for (String includePath : includes)
                {
                    try
                    {
                        File includeFolder = new File(includePath.trim());
                        if (!includeFolder.isFile() && includeFolder.exists())
                        {
                            List<File> childrenHeaders = new ArrayList<File>();
                            recursiveFindFileByExtension(includeFolder, childrenHeaders, "h");
                            for (File header : childrenHeaders)
                            {
                                IFile includeIFile = iFolder.getFile(new Path(header.getName()));
                                if (!includeIFile.exists())
                                {
                                    includeIFile.createLink(new Path(header.getAbsolutePath()), 0, new NullProgressMonitor());
                                }
                                else
                                {
                                    Log.warningLog("Warning, found redundant instance of file " + header.getName() + " in directory " + includeFolder.getName() + ". This instance will not be used");
                                }

                            }
                        }
                    }
                    catch (CoreException e)
                    {
                        Log.errorLog("Could not process header file " + includePath + " because of error " + e.getMessage());
                    }
                }
            }
            catch (CoreException e)
            {
                Log.errorLog("Could not process header files because of error " + e.getMessage());
            }

        }
    }

    protected void getCModels(List<String> cModels, IProject linkProject, List<IFile> modelIFiles)
    {
        if (cModels != null)
        {
            for (String cModelPath : cModels)
            {
                try
                {
                    File mainFile = new File(cModelPath.trim());
                    if (mainFile.exists() && mainFile.isFile())
                    {
                        File folder = mainFile.getParentFile();
                        IFolder iFolder = linkProject.getFolder(folder.getName());
                        if (!iFolder.exists())
                        {
                            iFolder.create(0, true, new NullProgressMonitor());
                        }

                        IFile mainIFile = iFolder.getFile(new Path(mainFile.getName()));
                        if (!mainIFile.exists())
                        {
                            mainIFile.createLink(new Path(mainFile.getAbsolutePath()), 0, new NullProgressMonitor());
                            modelIFiles.add(mainIFile);
                        }
                    }
                    else
                    {
                        Log.errorLog("Could not find C file " + cModelPath + " at the given path. Please check it and try again");
                    }
                }
                catch (CoreException e)
                {
                    Log.errorLog("Could not import C file " + cModelPath + " because of error " + e.getMessage());
                }
            }

        }
    }

    protected List<RuleSet> findAndInitRulesets(File rootFile, List<String> ruleSets, String xsdPath, String inhibitor, IProject linkProject)
    {
        List<RuleSet> result = new ArrayList<RuleSet>();

        List<File> ruleSetFiles = findRulesetFiles(rootFile, ruleSets, xsdPath, linkProject, result);

        List<Inhibition> inhibitions = getInhibitions(inhibitor);

        ResultsPackage.eINSTANCE.eClass();
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("ruleset", new XMIResourceFactoryImpl());
        ResourceSet set = new ResourceSetImpl();
        set.getLoadOptions().put(XMLResource.OPTION_DEFER_ATTACHMENT, true);
        set.getLoadOptions().put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, true);
        // if findRulesetFiles fails at any point it will return an empty list : this is safe
        for (File ruleSetFile : ruleSetFiles)
        {
            URI fileURI = URI.createFileURI(ruleSetFile.getAbsolutePath().toString());
            Resource resource = set.getResource(fileURI, true);
            for (EObject eobj : resource.getContents())
            {
                if (eobj instanceof RuleSet)
                {
                    RuleSet ruleSet = (RuleSet) eobj;
                    for (RuleFile ruleFile : ruleSet.getRuleFiles())
                    {
                        ruleFile.initRulesfromRuleset();
                        for (Rule rule : ruleFile.getRules())
                        {
                            rule.setIsActive(!isInhibited(ruleSet, ruleFile, rule, inhibitions));
                        }
                    }
                    result.add(ruleSet);
                }
                break;
            }
        }

        return result;
    }

    protected List<Inhibition> getInhibitions(String inhibitorPath)
    {
    	List<Inhibition> inhibitions = new ArrayList<Inhibition>();
    	if(inhibitorPath != null) {
    		File inhibitorFile = new File(inhibitorPath.trim());
    		if (inhibitorFile != null && inhibitorFile.exists())
    		{
    			try
    			{
    				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inhibitorFile)));
    				String readLine;
    				while ((readLine = in.readLine()) != null)
    				{
    					readLine = readLine.trim();
    					Inhibition inhibition = Inhibition.parse(readLine);
    					if (inhibition != null)
    					{
    						inhibitions.add(inhibition);
    					}
    				}
    				in.close();
    			}
    			catch (IOException e)
    			{
    				Log.errorLog("Could not read inhibitor file " + inhibitorFile.getName() + " because of error " + e.getMessage());
    			}
    		}
    	}
    	return inhibitions;
    }

    protected boolean isInhibited(RuleSet ruleSet, RuleFile ruleFile, Rule rule, List<Inhibition> inhibitions)
    {
        boolean result = false;
        for (Inhibition inhibition : inhibitions)
        {
            boolean ruleSetMatch = ruleSet.getName().equals(inhibition.getRuleSetName());
            boolean ruleFileMatch = ruleFile.getName().equals(inhibition.getRuleFileName());
            boolean ruleMatch = rule.getName().equals(inhibition.getRuleName());
            if (ruleSetMatch && ruleFileMatch && ruleMatch)
            {
                result = true;
                break;
            }
        }
        return result;
    }

    protected List<File> findRulesetFiles(File rootFile, List<String> ruleSets, String xsdPath, IProject linkProject, List<RuleSet> result)
    {
        List<File> ruleSetFiles = new ArrayList<File>();
        // Then create links, and add all specified rulesets
        if (ruleSets != null && !ruleSets.isEmpty())
        {
            for (String relativePath : ruleSets)
            {
                File ruleSetFile = new File(rootFile.getAbsolutePath() + relativePath.trim());
                if (!ruleSetFile.exists())
                {
                    Log.errorLog("Ruleset file " + relativePath + " could not be found. Please check the given path and try again");
                    return Collections.emptyList();
                }
                else
                {
                    ruleSetFiles.add(ruleSetFile);
                }
            }
        }

        // find all .xsd files (even those which are not the main xsd files, as they might be needed)
        List<File> xsds = new ArrayList<File>();
        recursiveFindFileByExtension(rootFile, xsds, "xsd");
        // Add the main xsdPath as a ruleset
        if (xsdPath != null)
        {
            File mainXSD = new File(rootFile.getAbsolutePath() + xsdPath.trim());
            if (!mainXSD.exists())
            {
                Log.errorLog("XSD file " + xsdPath + " could not be found. Please check the path and try again");
                return Collections.emptyList();
            }
            for (File file : xsds)
            {
                IFile f = linkProject.getFile(new Path(file.getName()));
                try
                {
                    f.createLink(new Path(file.getAbsolutePath()), 0, new NullProgressMonitor());
                    if (file.equals(mainXSD))
                    {
                        RuleSet constraintsRuleset = ResultsFactory.eINSTANCE.createRuleSet(file.getName(), "1.0", Collections.singletonList(f));
                        result.add(constraintsRuleset);
                    }
                }
                catch (CoreException e)
                {
                    Log.errorLog("XSD file " + file.getName() + " was found, but could not be retrieved because of error : " + e.getMessage());
                }
            }
        }
        return ruleSetFiles;
    }

    protected void recursiveFindFileByExtension(File parentFile, List<File> foundFiles, String extension)
    {
        File[] children = parentFile.listFiles();
        if (children != null)
        {
            for (File child : children)
            {
                if (child.isFile())
                {
                    String[] split = child.getName().split("\\.");
                    if (split.length > 1 && split[split.length - 1].equalsIgnoreCase(extension))
                    {
                        foundFiles.add(child);
                    }
                }
                recursiveFindFileByExtension(child, foundFiles, extension);
            }
        }
    }

    public void destroyLinkProjects()
    {
        IProject baselinkProject = ResourcesPlugin.getWorkspace().getRoot().getProject(LINK_PROJECT);
        if (baselinkProject.exists())
        {
            try
            {
                baselinkProject.delete(true, new NullProgressMonitor());
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }
        }
        IProject cLinkproject = ResourcesPlugin.getWorkspace().getRoot().getProject(C_LINK_PROJECT);
        if (cLinkproject.exists())
        {
            try
            {
                cLinkproject.delete(true, new NullProgressMonitor());
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected static class Inhibition
    {
        private String ruleSetName;

        private String ruleFileName;

        private String ruleName;

        public Inhibition(String ruleSetName, String ruleFileName, String ruleName)
        {
            super();
            this.ruleSetName = ruleSetName;
            this.ruleFileName = ruleFileName;
            this.ruleName = ruleName;
        }

        public static Inhibition parse(String line)
        {
            String[] split = line.split(":");
            if (split.length == 3)
            {
                return new Inhibition(split[0], split[1], split[2]);
            }
            else
            {
                return null;
            }
        }

        public String getRuleSetName()
        {
            return ruleSetName;
        }

        public String getRuleFileName()
        {
            return ruleFileName;
        }

        public String getRuleName()
        {
            return ruleName;
        }
    }

}
