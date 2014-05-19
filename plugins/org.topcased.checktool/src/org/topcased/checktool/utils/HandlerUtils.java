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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.topcased.checktool.Activator;
import org.topcased.checktool.Log;
import org.topcased.checktool.dialog.CheckModelDialog;
import org.topcased.checktool.dialog.CreateRulesetDialog;
import org.topcased.checktool.results.ResultSet;
import org.topcased.checktool.results.ResultsFactory;
import org.topcased.checktool.results.ResultsPackage;
import org.topcased.checktool.results.Rule;
import org.topcased.checktool.results.RuleFile;
import org.topcased.checktool.results.RuleFileResult;
import org.topcased.checktool.results.RuleSet;
import org.topcased.checktool.results.RuleSetResult;

public class HandlerUtils
{

    public static HandlerUtils instance = new HandlerUtils();

    public void doCheck(List<IFile> modelFiles, List<RuleSet> filteredRuleSets, String filePath) throws CoreException
    {
        // A ResourceSet that will be used throughout the checking process to load files
        // it'll have its registry contributed to by the C parsing methods
        ResourceSet resSet = new ResourceSetImpl();
        // resSet.getPackageRegistry().put(null, _2Package.eINSTANCE);
        // // resSet.getLoadOptions().put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, true);
        // resSet.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, new BasicExtendedMetaData(){
        //
        // @Override
        // public EPackage demandPackage(String namespace)
        // {
        // if (namespace == null){
        // return _2Package.eINSTANCE;
        // }
        // return super.demandPackage(namespace);
        // }
        //
        // });
        // XMLOptions xmlOptions = new XMLOptionsImpl();
        // xmlOptions.setProcessAnyXML(true);
        // // xmlOptions.setocessSchemaLocations(true);
        // resSet.getLoadOptions().put(XMLResource.OPTION_XML_OPTIONS, xmlOptions);
        // List of files that failed pre-processing.
        List<IFile> failedFiles = new ArrayList<IFile>();
        List<Resource> processedResources = new ArrayList<Resource>();
        for (IFile modelFile : modelFiles)
        {
            if ("c".equalsIgnoreCase(modelFile.getFullPath().getFileExtension()))
            {
                // replace it with its processed version
                Log.infoLog("Processing file " + modelFile.getName());
                Resource processed = processCModel(modelFile, resSet);
                if (processed == null)
                {
                    failedFiles.add(modelFile);
                }
                else
                {
                    processedResources.add(processed);
                }
            }
            else
            {
                URI uri = URI.createFileURI(modelFile.getLocation().toString());
                Resource resource = null;
                try
                {
                    resource = resSet.getResource(uri, true);
                }
                catch (RuntimeException e)
                {
                    e.printStackTrace();
                    resource = resSet.getResource(uri, false);
                }
                if (resource != null)
                {
                    processedResources.add(resource);
                }

            }
        }
        if (!failedFiles.isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append("The following .c files could not be parsed: ");
            for (IFile failedFile : failedFiles)
            {
                builder.append(failedFile.getLocation().toString());
                modelFiles.remove(failedFile);
            }
            throw new CoreException(new Status(IStatus.ERROR,Activator.PLUGIN_ID,builder.toString()));
        }

        // Create a ResultSet "skeleton"
        ResultSet resultSet = ResultsFactory.eINSTANCE.createResultSet();
        for (RuleSet set : getActiveRuleSets(new ArrayList<RuleSet>(filteredRuleSets)))
        {
            RuleSetResult rsResult = ResultsFactory.eINSTANCE.createRuleSetResult();
            rsResult.setRuleSet(set);
            for (RuleFile file : getActiveRuleFiles(set))
            {
                final String fileExtension = file.getFileExtension();
                AbstractRuleFileManager ruleFileManager = getAbstractRuleFileManager(fileExtension);

                if (ruleFileManager != null)
                {
                    Log.infoLog("Evaluating rule file " + file.getName());
                    RuleFileResult evaluatedRuleFileResult = ruleFileManager.evaluateRuleFile(file, processedResources);
                    if (evaluatedRuleFileResult != null)
                    {
                        rsResult.getRuleFileResults().add(evaluatedRuleFileResult);
                    }
                }
                resultSet.getRuleSetResults().add(rsResult);
            }

        }
        if (filePath != null)
        {
            try
            {
                URI fileURI = URI.createFileURI(new File(filePath).getAbsolutePath());
                Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
                Map<String, Object> m = reg.getExtensionToFactoryMap();
                m.put("result", new XMIResourceFactoryImpl());
                Resource resource = resSet.createResource(fileURI);
                resource.getContents().add(resultSet);
                resource.save(Collections.emptyMap());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void createRuleSet(List<IFile> files)
    {
        CreateRulesetDialog dialog = new CreateRulesetDialog(Display.getCurrent().getActiveShell(), "Create Ruleset", files);
        boolean isOK = (dialog.open() == Window.OK);
        if (isOK)
        {
            String name = dialog.getName();
            String version = dialog.getVersion();
            List<IFile> resultFiles = dialog.getFiles();

            RuleSet createdRuleSet = ResultsFactory.eINSTANCE.createRuleSet(name, version, resultFiles);
            FileDialog fDialog = new FileDialog(Display.getDefault().getActiveShell());
            fDialog.setFilterExtensions(new String[] {"ruleset"});
            String filePath = fDialog.open();
            if (filePath != null)
            {
                if (!filePath.endsWith(".ruleset"))
                {
                    filePath = filePath.concat(".ruleset");
                }
                try
                {
                    URI fileURI = URI.createFileURI(new File(filePath).getAbsolutePath());
                    ResourceSet resSet = new ResourceSetImpl();
                    Resource resource = resSet.createResource(fileURI);
                    resource.getContents().add(createdRuleSet);
                    resource.save(Collections.emptyMap());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            // refresh
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            for (IProject project : projects)
            {
                try
                {
                    project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                }
                catch (CoreException e)
                {
                    // Do nothing; this is not important
                }
            }
        }
    }

    public void checkModelGUI(List<IFile> files)
    {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        List<RuleSet> ruleSets = findAndInitRulesets(root);
        CheckModelDialog dialog = new CheckModelDialog(Display.getCurrent().getActiveShell(), "Check Models", files, ruleSets);
        boolean isOK = dialog.open() == Window.OK;
        if (!isOK)
        {
            return;
        }
        // Pre-process model files as required
        List<IFile> modelFiles = dialog.getModelFiles();
        // Retrieve the rulesets
        List<RuleSet> filteredRuleSets = dialog.getRuleSets();
        IFile constraintsFile = dialog.getContraintsFile();
        // add the constraints ruleset, if applicable
        if (constraintsFile != null)
        {
            RuleSet constraintsRuleset = ResultsFactory.eINSTANCE.createRuleSet(constraintsFile.getName(), "1.0", Collections.singletonList(constraintsFile));
            filteredRuleSets.add(constraintsRuleset);
        }
        // and the result path
        String filePath = dialog.getResultFilePath();

        try {
			doCheck(modelFiles, filteredRuleSets, filePath);
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Information", "Finished checking");
		} catch (CoreException e1) {
			e1.printStackTrace();
		}


        // refresh
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects)
        {
            try
            {
                project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            }
            catch (CoreException e)
            {
                // Do nothing; this is not important enough to handle
            }
        }
    }
    
    public AbstractRuleFileManager getAbstractRuleFileManager(final String fileExtension)
    {

        AbstractRuleFileManager ruleFileManager = null;
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.topcased.checktool.rulesets.rulefilemanager");
        for (IConfigurationElement element : elements)
        {
            if (fileExtension.equals(element.getAttribute("Extension")))
            {
                try
                {
                    ruleFileManager = ((AbstractRuleFileManager) element.createExecutableExtension("Manager"));
                }
                catch (CoreException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return ruleFileManager;
    }

    public List<RuleSet> findAndInitRulesets(IResource root)
    {
        List<RuleSet> ruleSets = new ArrayList<RuleSet>();
        final List<IResource> rulesetResources = new ArrayList<IResource>();
        try
        {
            root.accept(new IResourceVisitor()
            {
                public boolean visit(IResource resource) throws CoreException
                {
                    if (resource instanceof IContainer)
                    {
                        return true;
                    }
                    else if (resource instanceof IFile)
                    {
                        if (("ruleset").equals(resource.getFullPath().getFileExtension()))
                        {
                            rulesetResources.add(resource);
                        }
                    }
                    return false;
                }
            });
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }

        ResultsPackage.eINSTANCE.eClass();

        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("ruleset", new XMIResourceFactoryImpl());
        ResourceSet set = new ResourceSetImpl();
        set.getLoadOptions().put(XMLResource.OPTION_DEFER_ATTACHMENT, true);
        set.getLoadOptions().put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, true);
        for (IResource iRes : rulesetResources)
        {
            URI fileURI = URI.createURI(iRes.getLocationURI().toString());
            Resource resource = set.getResource(fileURI, true);
            for (EObject eobj : resource.getContents())
            {
                if (eobj instanceof RuleSet)
                {
                    for (RuleFile file : ((RuleSet) eobj).getRuleFiles())
                    {
                        file.initRulesfromRuleset();
                    }
                    ruleSets.add((RuleSet) eobj);
                }
                break;
            }
        }

        return ruleSets;
    }

    protected List<Rule> getActiveRules(Collection<RuleSet> sets)
    {
        List<Rule> rules = new ArrayList<Rule>();
        for (RuleSet set : sets)
        {
            for (RuleFile ruleFile : set.getRuleFiles())
            {
                for (Rule rule : ruleFile.getRules())
                {
                    if (rule.isIsActive())
                    {
                        rules.add(rule);
                    }
                }
            }
        }
        return rules;
    }

    protected List<RuleFile> getActiveRuleFiles(Collection<RuleSet> sets)
    {
        List<RuleFile> ruleFiles = new ArrayList<RuleFile>();
        for (RuleSet set : sets)
        {
            ruleFiles = getActiveRuleFiles(set);
        }
        return ruleFiles;
    }

    protected List<RuleFile> getActiveRuleFiles(RuleSet set)
    {
        List<RuleFile> ruleFiles = new ArrayList<RuleFile>();
        for (RuleFile ruleFile : set.getRuleFiles())
        {
            for (Rule rule : ruleFile.getRules())
            {
                if (rule.isIsActive())
                {
                    ruleFiles.add(ruleFile);
                    break;
                }
            }
        }
        return ruleFiles;
    }

    protected List<RuleSet> getActiveRuleSets(Collection<RuleSet> sets)
    {
        List<RuleSet> rulesets = new ArrayList<RuleSet>();
        for (RuleSet set : sets)
        {
            for (RuleFile ruleFile : set.getRuleFiles())
            {
                boolean found = false;
                for (Rule rule : ruleFile.getRules())
                {
                    if (rule.isIsActive())
                    {
                        rulesets.add(set);
                        found = true;
                        break;
                    }
                }
                if (found)
                {
                    break;
                }
            }
        }
        return rulesets;
    }

    public Resource processCModel(IFile modelFile, ResourceSet resSet)
    {
        return CUtils.getInstance().parseCFile(modelFile, resSet);
    }

}
