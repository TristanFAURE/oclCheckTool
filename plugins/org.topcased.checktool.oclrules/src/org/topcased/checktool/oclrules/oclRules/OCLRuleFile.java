/**
 * Copyright (c) 2014 AtoS.
 * 
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.topcased.checktool.oclrules.oclRules;

import org.eclipse.emf.common.util.EList;
import org.topcased.checktool.results.RuleFile;
import org.topcased.ocl.common.OCLResource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OCL Rule File</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.topcased.checktool.oclrules.oclRules.OclRulesPackage#getOCLRuleFile()
 * @model
 * @generated
 */
public interface OCLRuleFile extends RuleFile {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" dataType="org.topcased.checktool.oclrules.oclRules.OCLResource"
	 * @generated
	 */
	OCLResource getResource();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model resourcesMany="true"
	 * @generated
	 */
	void completeFiles(EList resources);
} // OCLRuleFile
