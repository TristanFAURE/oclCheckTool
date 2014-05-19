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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topcased.checktool.oclrules.oclRules.OCLRule;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.ecore.Constraint;
import org.topcased.checktool.Log;
import org.topcased.ocl.checker.engine.OCLAnnotationHandler;
import org.topcased.ocl.checker.engine.OCLCheckerEngineDelegate;
import org.topcased.ocl.checker.utils.OCLEvaluationUtil;
import org.topcased.ocl.common.OCLDocumentUtil;
import org.topcased.ocl.common.OCLResource;
import org.topcased.ocl.common.TopcasedOCL;
import org.topcased.ocl.evaluation.util.QueryTypeExtentMap;

import org.topcased.checktool.results.Result;
import org.topcased.checktool.results.ResultsFactory;
import org.topcased.checktool.results.Rule;
import org.topcased.checktool.results.RuleFileResult;

public class ChecktoolOCLCheckerEngine extends OCLCheckerEngineDelegate
{
    private static ChecktoolOCLCheckerEngine instance;

    private RuleFileResult rfResult;

    @Override
    protected void setBooleanResult(Constraint constraint, Query<EClassifier, EClass, EObject> query, OCLAnnotationHandler handler)
    {
        // This is called by the OCL Checker after a constraint has been evaluated
        // We add our hook here to add to the result model that was set during our first call
        Result result = null;
        // Get all the rules in the ruleFile and find the one matching this constraint
        for (Rule rule : rfResult.getRuleFile().getRules())
        {
            if (rule instanceof OCLRule && ((OCLRule) rule).getConstraintName().equals(constraint.getName()) && rule.isIsActive())
            {
                // This is the right rule. Try to find a corresponding rule or create it.
                for (Result testedResult : rfResult.getRuleResults())
                {
                    if (rule.equals(testedResult.getRule()))
                    {
                        result = testedResult;
                        break;
                    }
                }
                if (result == null)
                {
                    // Creation should only happen on first evaluation of a given constraint
                    result = ResultsFactory.eINSTANCE.createResult();
                    result.setRule(rule);
                    rfResult.getRuleResults().add(result);
                    break;
                }
            }
        }

        if (result != null)
        {
            Object[] objectsToEvaluate = OCLEvaluationUtil.getEObjectArray(query, oclHelper.getContextClassifier());
            Object[] evaluations = OCLEvaluationUtil.evaluate(query, oclHelper.getContextClassifier()).toArray();

            if (evaluations.length > 0)
            {
                if (objectsToEvaluate[0] instanceof EObject)
                {
                    result.setResourceName(((EObject) objectsToEvaluate[0]).eResource().getURI().lastSegment());
                }
            }

            for (int i = 0; i < evaluations.length; i++)
            {
                if (objectsToEvaluate[i] instanceof EObject)
                {
                    if (!(evaluations[i] instanceof Boolean) || "false".equals(evaluations[i].toString()))
                    {
                        EObject eObject = (EObject) objectsToEvaluate[i];
                        result.getFailedItems().add(getString(eObject));
                    }
                }
            }
            result.setResult(result.getFailedItems().isEmpty());
        }
    }

    private String getString(EObject eObject)
    {
        EClass clazz = eObject.eClass();
        if (clazz instanceof EDataType || clazz instanceof EEnumLiteral)
        {
            return eObject.toString();
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append(clazz.getName());
            for (EStructuralFeature feature : clazz.getEAllStructuralFeatures())
            {
                EClassifier type = feature.getEType();
                if ((type instanceof EDataType || clazz instanceof EEnumLiteral) && !feature.isMany())
                {
                    Object value = eObject.eGet(feature);
                    builder.append("\n");
                    String valueString = (value == null ? "null" : value.toString());
                    builder.append(feature.getName() + " - " + valueString);
                }
            }
            return builder.toString();
        }
    }

    public static ChecktoolOCLCheckerEngine getInstance()
    {
        if (instance == null)
        {
            instance = new ChecktoolOCLCheckerEngine();
        }
        return instance;
    }

    public void evaluateOCLRules(Resource resource, List<OCLResource> singletonList, boolean createMarkers, NullProgressMonitor nullProgressMonitor, RuleFileResult rfResult)
    {
        if (!(ocl.getExtentMap() instanceof QueryTypeExtentMap)){
            ocl.setExtentMap(new QueryTypeExtentMap(resource.getResourceSet()));
        }
        this.rfResult = rfResult;
        evaluateOCLRules(resource, singletonList, createMarkers, nullProgressMonitor);
    }

    @Override
    protected void initOCLEnvironment(EPackage metamodelPackage, ResourceSet set)
    {
        // Gets the OCL object
        ocl = TopcasedOCL.getOCL(metamodelPackage, set);
        oclHelper = ocl.createOCLHelper();

        // Build the extent map
        Map<EClass, Set< ? extends EObject>> extents = new QueryTypeExtentMap(set);
        ocl.setExtentMap(extents);
    }

    public List<Constraint> getRulesFromFile(List<Resource> processedResources, OCLResource file, String name)
    {
        try
        {
            InputStream inStream = file.getContent();
            OCLInput document = OCLDocumentUtil.getOCLDocument(inStream);
            URI mainNsURI = OCLDocumentUtil.getDocumentURI();
            EPackage metamodelPackage = EPackage.Registry.INSTANCE.getEPackage(mainNsURI.toString());
            if (metamodelPackage == null)
            {
                Log.warningLog("Could not find metamodel " + mainNsURI.toString() + ". OCL File " + name
                        + "could not be parsed. Ensure that any referenced C models are in the command line arguments and try again");
                return Collections.emptyList();
            }

            ResourceSet resourceSet = processedResources.iterator().next().getResourceSet();
            Map<String, Boolean> options = new HashMap<String, Boolean>();
            options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
            
            initOCLEnvironment(metamodelPackage,resourceSet);
            ocl.setExtentMap(new QueryTypeExtentMap(resourceSet));

            try
            {
                List<Constraint> constraints = ocl.parse(document);
                return constraints;
            }
            catch (ParserException e)
            {
                System.out.println(e);
            }
            finally
            {
                inStream.close();
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return Collections.emptyList();
    }

}
