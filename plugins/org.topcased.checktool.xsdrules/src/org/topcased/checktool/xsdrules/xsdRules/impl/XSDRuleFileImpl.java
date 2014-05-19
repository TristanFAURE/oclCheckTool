/**
 * Copyright (c) 2014 AtoS.
 * 
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.topcased.checktool.xsdrules.xsdRules.impl;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EClass;

import org.topcased.checktool.results.impl.RuleFileImpl;

import org.topcased.checktool.xsdrules.xsdRules.XSDRuleFile;
import org.topcased.checktool.xsdrules.xsdRules.XsdRulesPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>XSD Rule File</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class XSDRuleFileImpl extends RuleFileImpl implements XSDRuleFile {
	/**
     * @generated not
     */
    protected boolean isInit;

    /**
     * @generated not
     */
    protected IFile file;

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    protected XSDRuleFileImpl()
    {
		super();
	}

    /**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
    protected EClass eStaticClass()
    {
		return XsdRulesPackage.Literals.XSD_RULE_FILE;
	}

    public void initRulesfromRuleset()
    {
        throw new UnsupportedOperationException();
    }

    public void initRulesFromFile(IFile file)
    {
        this.file = file;
    }

    @Override
    public String getFileContents()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFileContents(String newFileContents)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @generated not
     */
    public String getFileExtension()
    {
    	 return "xsd";
	}

    /**
     * @generated not
     */
    public IFile getFile()
    {
    	return file;
	}

    /**
     * @generated not
     */
    public void setFile(IFile iFile)
    {
		this.file = iFile;
	}

} //XSDRuleFileImpl
