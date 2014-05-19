/**
 * Copyright (c) 2014 AtoS.
 * 
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.topcased.checktool.results;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Result Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.topcased.checktool.results.ResultSet#getRuleSetResults <em>Rule Set Results</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.topcased.checktool.results.ResultsPackage#getResultSet()
 * @model
 * @generated
 */
public interface ResultSet extends EObject {
	/**
	 * Returns the value of the '<em><b>Rule Set Results</b></em>' containment reference list.
	 * The list contents are of type {@link org.topcased.checktool.results.RuleSetResult}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rule Set Results</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rule Set Results</em>' containment reference list.
	 * @see org.topcased.checktool.results.ResultsPackage#getResultSet_RuleSetResults()
	 * @model containment="true"
	 * @generated
	 */
	EList<RuleSetResult> getRuleSetResults();

} // ResultSet
