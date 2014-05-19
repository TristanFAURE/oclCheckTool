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
 * A representation of the model object '<em><b>Rule File Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.topcased.checktool.results.RuleFileResult#getRuleResults <em>Rule Results</em>}</li>
 *   <li>{@link org.topcased.checktool.results.RuleFileResult#getRuleFile <em>Rule File</em>}</li>
 *   <li>{@link org.topcased.checktool.results.RuleFileResult#getRuleFileName <em>Rule File Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.topcased.checktool.results.ResultsPackage#getRuleFileResult()
 * @model
 * @generated
 */
public interface RuleFileResult extends EObject {
	/**
	 * Returns the value of the '<em><b>Rule Results</b></em>' containment reference list.
	 * The list contents are of type {@link org.topcased.checktool.results.Result}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rule Results</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rule Results</em>' containment reference list.
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFileResult_RuleResults()
	 * @model containment="true"
	 * @generated
	 */
	EList<Result> getRuleResults();

	/**
	 * Returns the value of the '<em><b>Rule File</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rule File</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rule File</em>' reference.
	 * @see #setRuleFile(RuleFile)
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFileResult_RuleFile()
	 * @model required="true"
	 * @generated
	 */
	RuleFile getRuleFile();

	/**
	 * Sets the value of the '{@link org.topcased.checktool.results.RuleFileResult#getRuleFile <em>Rule File</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rule File</em>' reference.
	 * @see #getRuleFile()
	 * @generated
	 */
	void setRuleFile(RuleFile value);

	/**
	 * Returns the value of the '<em><b>Rule File Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rule File Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rule File Name</em>' attribute.
	 * @see #setRuleFileName(String)
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFileResult_RuleFileName()
	 * @model derived="true"
	 * @generated
	 */
	String getRuleFileName();

	/**
	 * Sets the value of the '{@link org.topcased.checktool.results.RuleFileResult#getRuleFileName <em>Rule File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rule File Name</em>' attribute.
	 * @see #getRuleFileName()
	 * @generated
	 */
	void setRuleFileName(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean resultIsOK();

} // RuleFileResult
