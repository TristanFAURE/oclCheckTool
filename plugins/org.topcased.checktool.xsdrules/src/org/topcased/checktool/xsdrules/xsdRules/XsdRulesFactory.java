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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.topcased.checktool.xsdrules.xsdRules.XsdRulesPackage
 * @generated
 */
public interface XsdRulesFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	XsdRulesFactory eINSTANCE = org.topcased.checktool.xsdrules.xsdRules.impl.XsdRulesFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>XSD Rule File</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>XSD Rule File</em>'.
	 * @generated
	 */
	XSDRuleFile createXSDRuleFile();

	/**
	 * Returns a new object of class '<em>XSD Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>XSD Result</em>'.
	 * @generated
	 */
	XSDResult createXSDResult();

	/**
	 * Returns a new object of class '<em>XSD Rule</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>XSD Rule</em>'.
	 * @generated
	 */
	XSDRule createXSDRule();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	XsdRulesPackage getXsdRulesPackage();

} //XsdRulesFactory
