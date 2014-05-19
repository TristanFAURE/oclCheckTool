/**
 * Copyright (c) 2014 AtoS.
 * 
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.topcased.checktool.xsdrules.xsdRules;

import org.eclipse.core.resources.IFile;
import org.topcased.checktool.results.RuleFile;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>XSD Rule File</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.topcased.checktool.xsdrules.xsdRules.XsdRulesPackage#getXSDRuleFile()
 * @model
 * @generated
 */
public interface XSDRuleFile extends RuleFile {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getFileExtension();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" dataType="org.topcased.checktool.xsdrules.xsdRules.IFile"
	 * @generated
	 */
	IFile getFile();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model iFileDataType="org.topcased.checktool.xsdrules.xsdRules.IFile"
	 * @generated
	 */
	void setFile(IFile iFile);
} // XSDRuleFile
