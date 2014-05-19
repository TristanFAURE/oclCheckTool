/**
 * Copyright (c) 2014 AtoS.
 * 
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.topcased.checktool.results.impl;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.topcased.checktool.results.*;
import org.topcased.checktool.results.CMetamodel;
import org.topcased.checktool.results.Configuration;
import org.topcased.checktool.results.Result;
import org.topcased.checktool.results.ResultSet;
import org.topcased.checktool.results.ResultsFactory;
import org.topcased.checktool.results.ResultsPackage;
import org.topcased.checktool.results.RuleFile;
import org.topcased.checktool.results.RuleFileResult;
import org.topcased.checktool.results.RuleSet;
import org.topcased.checktool.results.RuleSetResult;
import org.topcased.checktool.utils.AbstractRuleFileManager;
import org.topcased.checktool.utils.HandlerUtils;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ResultsFactoryImpl extends EFactoryImpl implements ResultsFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public static ResultsFactory init()
    {
		try {
			ResultsFactory theResultsFactory = (ResultsFactory)EPackage.Registry.INSTANCE.getEFactory("http:///www.topcased.org/checktool/results.ecore"); 
			if (theResultsFactory != null) {
				return theResultsFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ResultsFactoryImpl();
	}

    /**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public ResultsFactoryImpl()
    {
		super();
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    @Override
    public EObject create(EClass eClass)
    {
		switch (eClass.getClassifierID()) {
			case ResultsPackage.RULE_SET: return createRuleSet();
			case ResultsPackage.CMETAMODEL: return createCMetamodel();
			case ResultsPackage.CONFIGURATION: return createConfiguration();
			case ResultsPackage.RESULT_SET: return createResultSet();
			case ResultsPackage.RULE_SET_RESULT: return createRuleSetResult();
			case ResultsPackage.RESULT: return createResult();
			case ResultsPackage.RULE_FILE_RESULT: return createRuleFileResult();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public RuleSet createRuleSet()
    {
		RuleSetImpl ruleSet = new RuleSetImpl();
		return ruleSet;
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public CMetamodel createCMetamodel()
    {
		CMetamodelImpl cMetamodel = new CMetamodelImpl();
		return cMetamodel;
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public Configuration createConfiguration()
    {
		ConfigurationImpl configuration = new ConfigurationImpl();
		return configuration;
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public ResultSet createResultSet()
    {
		ResultSetImpl resultSet = new ResultSetImpl();
		return resultSet;
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public RuleSetResult createRuleSetResult()
    {
		RuleSetResultImpl ruleSetResult = new RuleSetResultImpl();
		return ruleSetResult;
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public Result createResult()
    {
		ResultImpl result = new ResultImpl();
		return result;
	}

    /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public RuleFileResult createRuleFileResult()
    {
		RuleFileResultImpl ruleFileResult = new RuleFileResultImpl();
		return ruleFileResult;
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    public ResultsPackage getResultsPackage()
    {
		return (ResultsPackage)getEPackage();
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
    @Deprecated
    public static ResultsPackage getPackage()
    {
		return ResultsPackage.eINSTANCE;
	}

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated not
     */
    public RuleSet createRuleSet(String name, String version, List<IFile> ruleIFiles)
    {
        RuleSet ruleSet = createRuleSet();
        ruleSet.setName(name);
        ruleSet.setVersion(version);

        for (IFile iFile : ruleIFiles)
        {
            final String fileExtension = iFile.getFileExtension();
            AbstractRuleFileManager ruleFileManager = HandlerUtils.instance.getAbstractRuleFileManager(fileExtension);
            if(ruleFileManager != null) {
                RuleFile rFile = ruleFileManager.createRuleFile(iFile);
                ruleSet.getRuleFiles().add(rFile);
            }
        }

        return ruleSet;
    }

} //ResultsFactoryImpl
