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
 * A representation of the model object '<em><b>Rule File</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.topcased.checktool.results.RuleFile#getRules <em>Rules</em>}</li>
 *   <li>{@link org.topcased.checktool.results.RuleFile#getName <em>Name</em>}</li>
 *   <li>{@link org.topcased.checktool.results.RuleFile#getFileContents <em>File Contents</em>}</li>
 *   <li>{@link org.topcased.checktool.results.RuleFile#isIsValid <em>Is Valid</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.topcased.checktool.results.ResultsPackage#getRuleFile()
 * @model abstract="true"
 * @generated
 */
public interface RuleFile extends EObject {
	/**
	 * Returns the value of the '<em><b>Rules</b></em>' containment reference list.
	 * The list contents are of type {@link org.topcased.checktool.results.Rule}.
	 * It is bidirectional and its opposite is '{@link org.topcased.checktool.results.Rule#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Rules</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rules</em>' containment reference list.
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFile_Rules()
	 * @see org.topcased.checktool.results.Rule#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<Rule> getRules();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFile_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.topcased.checktool.results.RuleFile#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>File Contents</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Contents</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Contents</em>' attribute.
	 * @see #setFileContents(String)
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFile_FileContents()
	 * @model
	 * @generated
	 */
	String getFileContents();

	/**
	 * Sets the value of the '{@link org.topcased.checktool.results.RuleFile#getFileContents <em>File Contents</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Contents</em>' attribute.
	 * @see #getFileContents()
	 * @generated
	 */
	void setFileContents(String value);

	/**
	 * Returns the value of the '<em><b>Is Valid</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Valid</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Valid</em>' attribute.
	 * @see #setIsValid(boolean)
	 * @see org.topcased.checktool.results.ResultsPackage#getRuleFile_IsValid()
	 * @model default="true"
	 * @generated
	 */
	boolean isIsValid();

	/**
	 * Sets the value of the '{@link org.topcased.checktool.results.RuleFile#isIsValid <em>Is Valid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Valid</em>' attribute.
	 * @see #isIsValid()
	 * @generated
	 */
	void setIsValid(boolean value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void initRulesfromRuleset();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getFileExtension();

} // RuleFile
