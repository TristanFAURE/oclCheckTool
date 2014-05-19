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

import org.topcased.checktool.results.Result;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>XSD Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.topcased.checktool.xsdrules.xsdRules.XSDResult#getErrorMessage <em>Error Message</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.topcased.checktool.xsdrules.xsdRules.XsdRulesPackage#getXSDResult()
 * @model
 * @generated
 */
public interface XSDResult extends Result {
	/**
	 * Returns the value of the '<em><b>Error Message</b></em>' attribute.
	 * The default value is <code>"\"\""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Error Message</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Error Message</em>' attribute.
	 * @see #setErrorMessage(String)
	 * @see org.topcased.checktool.xsdrules.xsdRules.XsdRulesPackage#getXSDResult_ErrorMessage()
	 * @model default="\"\""
	 * @generated
	 */
	String getErrorMessage();

	/**
	 * Sets the value of the '{@link org.topcased.checktool.xsdrules.xsdRules.XSDResult#getErrorMessage <em>Error Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Error Message</em>' attribute.
	 * @see #getErrorMessage()
	 * @generated
	 */
	void setErrorMessage(String value);

} // XSDResult
