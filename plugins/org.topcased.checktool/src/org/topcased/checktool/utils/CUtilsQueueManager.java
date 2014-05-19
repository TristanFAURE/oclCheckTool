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
package org.topcased.checktool.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.parser.scanner.Token;
import org.eclipse.cdt.internal.core.parser.scanner.TokenWithImage;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

public class CUtilsQueueManager
{
    public enum TypeDefType {
        ENUM, STRUCT, SDECL;

        public static TypeDefType getType(IASTDeclSpecifier specifier)
        {
            if (specifier instanceof IASTEnumerationSpecifier)
            {
                return ENUM;
            }
            else if (specifier instanceof IASTCompositeTypeSpecifier)
            {
                return STRUCT;
            }
            else if (specifier instanceof IASTSimpleDeclSpecifier || specifier instanceof IASTNamedTypeSpecifier)
            {
                return SDECL;
            }
            else
            {
                return null;
            }
        }
    }

    protected static Map<String, EClassifier> typeRegistry = new HashMap<String, EClassifier>();

    protected static Map<String, Object> instanceRegistry = new HashMap<String, Object>();

    protected static List<EClass> tabClasses = new ArrayList<EClass>();

    protected static CUtilsQueueManager instance;

    protected List<Typedef> typeDefQueue = new LinkedList<Typedef>();

    protected List<SimpleDeclaration> declarationQueue = new LinkedList<SimpleDeclaration>();

    protected List<Initialization> initQueue = new LinkedList<Initialization>();

    protected EFactory factory;

    public static CUtilsQueueManager getInstance()
    {
        if (instance == null)
        {
            instance = new CUtilsQueueManager();
        }
        return instance;
    }

    public void addTypeDefPPA(IASTDeclaration declaration, TypeDefType type)
    {
        typeDefQueue.add(new Typedef(declaration, type));
    }

    public void addDeclarationPPA(String parentName, IASTDeclaration declaration)
    {
        declarationQueue.add(new SimpleDeclaration(parentName, declaration));
    }

    public void addDeclarationPPA(String parentName, IASTDeclaration declaration, String nameOverride)
    {
        declarationQueue.add(new SimpleDeclaration(parentName, declaration, nameOverride));
    }

    public void addInitializationPPA(String sourceName, String declaredName, IASTDeclaration declaration, boolean isPointer, String subTypeName)
    {
        initQueue.add(new Initialization(sourceName, declaredName, declaration, subTypeName, isPointer));
    }

    /**
     * Starting with a map with just the root name and empty classifier, fills it in. The resulting EClassifiers will
     * still need to be added to the EPackage
     * 
     * @param types
     */
    public void finishMetamodel(Map<String, EClassifier> types)
    {
        if (!types.isEmpty())
        {
            typeRegistry = types;
            for (PPA p : typeDefQueue)
            {
                p.run();
            }
            for (PPA p : declarationQueue)
            {
                p.run();
            }
            typeDefQueue.clear();
            declarationQueue.clear();
        }
    }

    /**
     * Takes an empty instance of the root EObject and fills it out Also clears out remaining static data and prepares
     * class for next parsing operation if applicable
     * 
     * @param rootName
     * @param rootEObject
     */
    public void finishModel(String rootName, EObject rootEObject, EPackage dynamicPackage)
    {
        factory = dynamicPackage.getEFactoryInstance();
        instanceRegistry.put(rootName, rootEObject);
        for (PPA p : initQueue)
        {
            p.run();
        }
        initQueue.clear();
        typeRegistry.clear();
        instanceRegistry.clear();
        tabClasses.clear();
    }

    protected Class< ? > getClass(int type)
    {
        switch (type)
        {
            case IASTSimpleDeclSpecifier.t_unspecified:
            case IASTSimpleDeclSpecifier.t_int:
                return Integer.class;
            case IASTSimpleDeclSpecifier.t_float:
                return Float.class;
            case IASTSimpleDeclSpecifier.t_double:
                return Double.class;
            case IASTSimpleDeclSpecifier.t_bool:
                return Boolean.class;
            case IASTSimpleDeclSpecifier.t_char:
            case IASTSimpleDeclSpecifier.t_wchar_t:
            case IASTSimpleDeclSpecifier.t_char16_t:
            case IASTSimpleDeclSpecifier.t_char32_t:
                return Character.class;
            default:
        }
        return null;
    }

    protected EDataType getEDataType(int type)
    {
        switch (type)
        {
            case IASTSimpleDeclSpecifier.t_int:
                return EcorePackage.Literals.EINT;
            case IASTSimpleDeclSpecifier.t_float:
                return EcorePackage.Literals.EFLOAT;
            case IASTSimpleDeclSpecifier.t_double:
                return EcorePackage.Literals.EDOUBLE;
            case IASTSimpleDeclSpecifier.t_bool:
                return EcorePackage.Literals.EBOOLEAN;
            case IASTSimpleDeclSpecifier.t_char:
            case IASTSimpleDeclSpecifier.t_wchar_t:
            case IASTSimpleDeclSpecifier.t_char16_t:
            case IASTSimpleDeclSpecifier.t_char32_t:
                return EcorePackage.Literals.ECHAR;
            default:
        }
        return null;
    }

    protected String getEmptyInitString(EDataType type)
    {
        if (type instanceof EEnum)
        {
            EEnum eEnum = (EEnum) type;
            if (eEnum.getELiterals().size() > 0)
            {
                return eEnum.getELiterals().get(0).getName();
            }
            else
            {
                return null;
            }
        }
        if (Integer.class.equals(type.getInstanceClass()))
        {
            return "0";
        }
        else if (Float.class.equals(type.getInstanceClass()))
        {
            return "0";
        }
        else if (Double.class.equals(type.getInstanceClass()))
        {
            return "0";
        }
        else if (Boolean.class.equals(type.getInstanceClass()))
        {
            return "false";
        }
        return null;
    }

    @SuppressWarnings("restriction")
    protected String getNodeName(IASTNode node)
    {
        if (node instanceof IASTNamedTypeSpecifier)
        {
            return ((IASTNamedTypeSpecifier) node).getName().toString();
        }
        try
        {
            return getImageToken(node.getSyntax()).getImage();
        }
        catch (ExpansionOverlapsBoundaryException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("restriction")
    protected TokenWithImage getImageToken(IToken token)
    {
        if (token instanceof TokenWithImage && !(token.getImage().equals("typedef")))
        {
            return (TokenWithImage) token;
        }
        else
        {
            final IToken next = token.getNext();
            if (next instanceof Token)
            {
                return getImageToken((IToken) next);
            }
            else
            {
                return null;
            }
        }
    }

    protected String getTabName(IASTDeclSpecifier s)
    {
        int i = 0;
        String candidateName = getNodeName(s) + "Tab" + 0;
        while (typeRegistry.containsKey(candidateName))
        {
            i++;
            candidateName = getNodeName(s) + "Tab" + i;
        }
        return candidateName;
    }

    public static IASTDeclSpecifier getDeclarationSpecifier(IASTDeclaration declaration)
    {
        IASTNode[] children = declaration.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            final IASTNode child = children[i];
            if (child instanceof IASTDeclSpecifier)
            {
                return (IASTDeclSpecifier) child;
            }
        }
        return null;
    }

    public static IASTDeclarator getDeclarationDeclarator(IASTDeclaration declaration)
    {
        IASTNode[] children = declaration.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            final IASTNode child = children[i];
            if (child instanceof IASTDeclarator)
            {
                return (IASTDeclarator) child;
            }
        }
        return null;
    }

    public static IASTInitializer getDeclaratorInitializer(IASTDeclarator declarator)
    {
        IASTNode[] children = declarator.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            final IASTNode child = children[i];
            if (child instanceof IASTInitializer)
            {
                return (IASTInitializer) child;
            }
        }
        return null;
    }

    private Object parseLiteral(String litValue, Class expectedClass)
    {
        Object replacementLiteral = null;
        if (expectedClass.equals(Float.class) && Float.valueOf(litValue) != null)
        {
            replacementLiteral = Float.valueOf(litValue);
        }
        else if (expectedClass.equals(Double.class) && Double.valueOf(litValue) != null)
        {
            replacementLiteral = Double.valueOf(litValue);
        }
        else if (expectedClass.equals(Integer.class) && Integer.decode(litValue) != null)
        {
            replacementLiteral = Integer.decode(litValue);
        }
        else if (expectedClass.equals(Boolean.class) && Boolean.valueOf(litValue) != null)
        {
            replacementLiteral = Boolean.valueOf(litValue);
        }
        else if (expectedClass.equals(String.class))
        {
            replacementLiteral = litValue;
        }
        return replacementLiteral;
    }

    protected interface PPA
    {
        void run();
    }

    protected abstract class Declaration implements PPA
    {
        protected IASTDeclaration declaration;

        protected Declaration(IASTDeclaration declaration)
        {
            this.declaration = declaration;
        }
    }

    protected class Typedef extends Declaration
    {
        private final TypeDefType type;

        public Typedef(IASTDeclaration declaration, TypeDefType type)
        {
            super(declaration);
            this.type = type;
        }

        public void run()
        {
            if (type == TypeDefType.STRUCT)
            {
                runStruct();
            }
            else if (type == TypeDefType.ENUM)
            {
                runEnum();
            }
            else if (type == TypeDefType.SDECL)
            {
                runSimple();
            }

        }

        private void runSimple()
        {
            IASTDeclarator declarator = getDeclarationDeclarator(declaration);
            IASTDeclSpecifier specifier = getDeclarationSpecifier(declaration);
            // If this is a simple, non-array type, define this as a datatype
            if (declarator != null && !(declarator instanceof IASTArrayDeclarator) && specifier instanceof IASTSimpleDeclSpecifier)
            {
                String typeName = getNodeName(declarator);
                if (typeName != null)
                {
                    EDataType dataType = EcoreFactory.eINSTANCE.createEDataType();
                    dataType.setName(typeName);
                    dataType.setInstanceClass(instance.getClass(((IASTSimpleDeclSpecifier) specifier).getType()));
                    typeRegistry.put(typeName, dataType);
                }
            }
            else
            {
                if (declarator != null && specifier != null)
                {
                    String typeName = getNodeName(declarator);
                    if (typeName != null)
                    {
                        EClass clazz = EcoreFactory.eINSTANCE.createEClass();
                        clazz.setName(typeName);
                        typeRegistry.put(typeName, clazz);
                        addDeclarationPPA(typeName, declaration, "element");
                    }
                }
            }
        }

        private void runStruct()
        {
            IASTDeclarator declarator = getDeclarationDeclarator(declaration);
            // This is a struct so we know this specifier is of type IASTCompositeType
            IASTCompositeTypeSpecifier specifier = (IASTCompositeTypeSpecifier) getDeclarationSpecifier(declaration);
            if (declarator != null && specifier != null)
            {
                String typeName = getNodeName(declarator);
                if (typeName != null)
                {
                    EClass clazz = EcoreFactory.eINSTANCE.createEClass();
                    clazz.setName(typeName);
                    typeRegistry.put(typeName, clazz);
                    for (IASTDeclaration memberDecl : (specifier).getMembers())
                    {
                        addDeclarationPPA(typeName, memberDecl);
                    }
                }
            }
        }

        private void runEnum()
        {
            IASTDeclarator declarator = getDeclarationDeclarator(declaration);
            // This is an enum so we know this specifier is of type IASTEnumerationSpecifier
            IASTEnumerationSpecifier specifier = (IASTEnumerationSpecifier) getDeclarationSpecifier(declaration);
            EEnum eEnum = EcoreFactory.eINSTANCE.createEEnum();
            // cases where declarator is empty should still be accounted for. It will not be able to be referenced as a
            // type, but its individual literals still can
            if (declarator != null)
            {
                String typeName = getNodeName(declarator);
                if (typeName != null)
                {
                    eEnum.setName(typeName);
                    typeRegistry.put(typeName, eEnum);
                }
            }
            if (specifier != null)
            {
                for (IASTEnumerator enumerator : specifier.getEnumerators())
                {
                    EEnumLiteral enumLit = EcoreFactory.eINSTANCE.createEEnumLiteral();
                    String enumLitName = enumerator.getName().toString();
                    enumLit.setName(enumLitName);

                    try
                    {
                        IASTExpression expressionValue = enumerator.getValue();
                        if (expressionValue != null)
                        {
                            int enumValue = Integer.decode(expressionValue.toString());
                            enumLit.setValue(enumValue);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        // Do nothing; this value will simply be set to null
                    }

                    eEnum.getELiterals().add(enumLit);
                    instanceRegistry.put(enumLitName, enumLit);
                }
            }
        }
    }

    /**
     * Declaration class for one-line declarations; this encompasses both "normal" and embedded declarations
     */
    protected class SimpleDeclaration extends Declaration
    {
        protected final String parentName;

        // nameOverride is used in certain instances where
        // the declarator name is not to be used
        // such as typeDef Complex1 Complex2;
        // being interpreted as a typedef struct {Complex1 element;} Complex2;
        protected final String nameOverride;

        public SimpleDeclaration(String parentName, IASTDeclaration declaration)
        {
            this(parentName, declaration, null);
        }

        public SimpleDeclaration(String parentName, IASTDeclaration declaration, String nameOverride)
        {
            super(declaration);
            this.parentName = parentName;
            this.nameOverride = nameOverride;
        }

        public void run()
        {
            Integer cardinality = null;
            String sourceName = null;
            String declaratedName = null;

            IASTDeclarator declarator = getDeclarationDeclarator(declaration);
            boolean isArray = declarator instanceof IASTArrayDeclarator;
            IASTDeclSpecifier s = getDeclarationSpecifier(declaration);

            if (s == null)
            {
                return;
            }
            else if (declarator == null)
            {
                // Sometimes a declaration has no declarator (and therefore cannot be named or added to the metamodel /
                // model) but still contains info we want to parse. Known cases are restricted to non typedef-ed enums
                parseChildren(s);
                return;
            }
            final String declaratorName = nameOverride == null ? declarator.getName().toString() : nameOverride;

            IASTPointerOperator[] pointerOperators = declarator.getPointerOperators();
            boolean isPointer = pointerOperators != null && pointerOperators.length > 0;

            if (isArray)
            {
                // create the tab class and link them together
                EClass tabClazz = EcoreFactory.eINSTANCE.createEClass();
                final String tabName = instance.getTabName(s);
                tabClazz.setName(tabName);
                typeRegistry.put(tabName, tabClazz);
                tabClasses.add(tabClazz);
                addLink(parentName, declaratorName, tabClazz, 1, isPointer);

                // find the cardinality and set the remaining variables
                IASTArrayDeclarator arrayDeclarator = (IASTArrayDeclarator) declarator;
                for (IASTArrayModifier mod : arrayDeclarator.getArrayModifiers())
                {
                    if (mod.getConstantExpression() instanceof IASTLiteralExpression)
                    {
                        IASTLiteralExpression le = (IASTLiteralExpression) mod.getConstantExpression();
                        cardinality = Integer.parseInt(new String(le.getValue()));
                    }
                }
                if (cardinality == null)
                {
                    cardinality = -1;
                }
                sourceName = tabName;
                declaratedName = "element";
            }
            else
            {
                sourceName = parentName;
                cardinality = 1;
                declaratedName = declaratorName;
            }
            if (sourceName != null && declaratedName != null)
            {
                // if getNodeLength == 0, this is an empty specifier, such as you might find for a statement like a = 1;
                // In this case, it's already been declared and therefore should not be declared again
                if (s.getFileLocation().getNodeLength() != 0)
                {
                    runSimpleDec(declaratedName, s, sourceName, cardinality, isPointer);
                }
                // Regardless of whether we initialize with an actual value or not, add an init statement.
                // The statement is different in case of an array
                if (isArray)
                {
                    addInitializationPPA(parentName, declaratorName, declaration, isPointer, sourceName);
                }
                else
                {
                    addInitializationPPA(sourceName, declaratedName, declaration, isPointer, null);
                }
            }

        }

        // Check for any elements that might need to be added to the instance registry
        private void parseChildren(IASTDeclSpecifier s)
        {
            for (IASTNode node : s.getChildren())
            {
                if (node instanceof IASTEnumerator)
                {
                    IASTEnumerator enumerator = (IASTEnumerator) node;
                    IASTExpression value = enumerator.getValue();
                    if (value instanceof IASTIdExpression)
                    {
                        Object object = instanceRegistry.get(((IASTIdExpression) value).getName().toString());
                        if (object != null)
                        {
                            instanceRegistry.put(enumerator.getName().toString(), object);
                        }
                    }
                    else if (value instanceof IASTLiteralExpression)
                    {
                        String stringValue = String.copyValueOf(((IASTLiteralExpression) value).getValue());
                        instanceRegistry.put(enumerator.getName().toString(), parseLiteral(stringValue, Integer.class));
                    }
                }
            }
        }

        private void runSimpleDec(String declaratedName, IASTDeclSpecifier s, String sourceName, int cardinality, boolean isPointer)
        {
            // e.g: { int a; }
            if (s instanceof IASTSimpleDeclSpecifier)
            {
                IASTSimpleDeclSpecifier specifier = (IASTSimpleDeclSpecifier) s;
                EDataType eType = instance.getEDataType(specifier.getType());
                addAttribute(sourceName, declaratedName, eType, cardinality);
            }
            else if (s instanceof IASTNamedTypeSpecifier)
            {
                IASTNamedTypeSpecifier specifier = (IASTNamedTypeSpecifier) s;
                EClassifier classifier = typeRegistry.get(specifier.getName().toString());
                // e.g: { Int18 a; }
                if (classifier instanceof EDataType)
                {
                    addAttribute(sourceName, declaratedName, (EDataType) classifier, cardinality);
                }
                // e.g: { ComplexType b; }
                else if (classifier instanceof EClass)
                {
                    addLink(sourceName, declaratedName, (EClass) classifier, cardinality, isPointer);
                }
            }
        }

        private void addLink(String sourceName, String declaratedName, EClass targetClazz, int cardinality, boolean isPointer)
        {
            EClassifier classifier = typeRegistry.get(sourceName);
            if (classifier instanceof EClass)
            {
                EClass sourceClazz = (EClass) classifier;
                EReference ref = EcoreFactory.eINSTANCE.createEReference();
                ref.setName(declaratedName);
                ref.setContainment(!isPointer);
                ref.setLowerBound(0);
                ref.setUpperBound(cardinality);
                ref.setEType(targetClazz);
                ref.setUnique(false);
                sourceClazz.getEStructuralFeatures().add(ref);
            }
        }

        private void addAttribute(String sourceName, String declaratedName, EDataType eType, int cardinality)
        {
            EClassifier classifier = typeRegistry.get(sourceName);
            if (classifier instanceof EClass)
            {
                EAttribute att = EcoreFactory.eINSTANCE.createEAttribute();
                att.setName(declaratedName);
                att.setEType(eType);
                att.setUpperBound(cardinality);
                att.setUnique(false);
                ((EClass) classifier).getEStructuralFeatures().add(att);
            }
        }

    }

    public class Initialization implements PPA
    {
        private final String sourceName;

        private final IASTDeclaration declaration;

        private final String declaredName;

        private String subTypeName;

        private boolean isPointer;

        /**
         * @param instanceName parent name
         * @param declaredName name of this particular instance
         * @param declaration the associated declaration
         */
        public Initialization(String instanceName, String declaredName, IASTDeclaration declaration, String subTypeName, boolean isPointer)
        {
            this.sourceName = instanceName;
            this.declaredName = declaredName;
            this.declaration = declaration;
            this.subTypeName = subTypeName;
            this.isPointer = isPointer;
        }

        public void run()
        {

            // Retrieve type's EClass (and structural features) using Specifier
            IASTDeclSpecifier s = getDeclarationSpecifier(declaration);
            IASTDeclarator declarator = getDeclarationDeclarator(declaration);
            IASTInitializer init = getDeclaratorInitializer(declarator);

            EClassifier classifier = null;
            // if this is an array, get the class (XTABY) to instance directly
            if (subTypeName != null)
            {
                classifier = typeRegistry.get(subTypeName);
            }
            // Otherwise use the declaration specifier
            else
            {
                // e.g: { int a; }
                if (s instanceof IASTSimpleDeclSpecifier)
                {
                    IASTSimpleDeclSpecifier specifier = (IASTSimpleDeclSpecifier) s;
                    classifier = instance.getEDataType(specifier.getType());
                }
                else if (s instanceof IASTNamedTypeSpecifier)
                {
                    IASTNamedTypeSpecifier specifier = (IASTNamedTypeSpecifier) s;
                    classifier = typeRegistry.get(specifier.getName().toString());
                }
            }

            // Retrieve parent
            Object parent = instanceRegistry.get(sourceName);
            if (parent == null || !(parent instanceof EObject))
            {
                return;
            }
            EObject parentEObject = (EObject) parent;
            EStructuralFeature feature = parentEObject.eClass().getEStructuralFeature(declaredName);

            if (isPointer && init != null && init instanceof IASTEqualsInitializer)
            {
                IASTEqualsInitializer equalsInit = (IASTEqualsInitializer) init;
                IASTInitializerClause clause = equalsInit.getInitializerClause();
                handlePointerClause(clause, parentEObject, feature);
            }
            else
            {
                Object thisObject = instanceRegistry.get(declaredName);
                // If this object does not already exist, initialize it and link it to its parent
                if (thisObject == null)
                {
                    thisObject = emptyInit(parentEObject, parentEObject.eClass(), classifier);
                    if(thisObject != null) {
                    	instanceRegistry.put(declaredName, thisObject);
                    	parentEObject.eSet(feature, thisObject);
                    }
                }
                // Then, init attributes according to any IASTInitializers found
                if (thisObject != null && init != null)
                {
                    /**
                     * call recursive assignment function. for value{ if array/struct, recursive call -> figure out
                     * EClass of type using feature as hint; else if literal value assign feature; else if named value
                     * find in instance registry and assign feature }
                     **/
                    // recursiveInit(Object object, IASTInitializer init)
                    // Get declarator value
                    if (init instanceof IASTEqualsInitializer)
                    {
                        IASTEqualsInitializer equalsInit = (IASTEqualsInitializer) init;
                        IASTInitializerClause clause = equalsInit.getInitializerClause();
                        processClause(clause, thisObject, parentEObject, feature);
                    }
                }
            }
        }

        private void handlePointerClause(IASTInitializerClause clause, EObject parentEObject, EStructuralFeature feature)
        {
            if (clause instanceof IASTIdExpression)
            {
                String instanceName = ((IASTIdExpression) clause).getName().toString();
                Object instance = instanceRegistry.get(instanceName);
                if (instance != null)
                {
                    parentEObject.eSet(feature, instance);
                    instanceRegistry.put(feature.getName(), instance);
                }
            }
            else if (clause instanceof IASTUnaryExpression)
            {
                IASTExpression expression = ((IASTUnaryExpression) clause).getOperand();
                handlePointerClause(expression, parentEObject, feature);
            }
        }

        /**
         * Entry point for the recursiveProcessClause method
         */
        private void processClause(IASTInitializerClause clause, Object thisObject, EObject parentEObject, EStructuralFeature feature)
        {
            recursiveProcessClause(clause, thisObject, parentEObject, feature, true, 0);
        }

        private void recursiveProcessClause(IASTInitializerClause clause, Object thisObject, EObject parentEObject, EStructuralFeature feature, boolean addToMap, int index)
        {
            if (clause instanceof IASTLiteralExpression)
            {
                handleLiteralExpressionClause(clause, thisObject, parentEObject, feature, addToMap, index);
            }
            // eg struct x y = { .z=4, .t = 3 };
            else if (clause instanceof ICASTDesignatedInitializer)
            {
                handleDesignedInitializer(clause, thisObject, parentEObject);
            }
            else if (clause instanceof IASTIdExpression)
            {
                handleIdExpressionClause(clause, thisObject, parentEObject, feature);
            }
            else if (clause instanceof IASTInitializerList)
            {
                handleInitializerList(clause, thisObject);
            }
            else if (clause instanceof IASTBinaryExpression)
            {
                handleBinaryExpressions(clause, thisObject, parentEObject, feature, addToMap, index);
            }
        }

        private void handleBinaryExpressions(IASTInitializerClause clause, Object thisObject, EObject parentEObject, EStructuralFeature feature, boolean addToMap, int index)
        {
            IASTBinaryExpression binary = (IASTBinaryExpression) clause;
            // XXX We'll only handle the expressions involving the addition of integers (or longs, floats, etc..)
            // This is subject to change.
            if (binary.getOperator() == IASTBinaryExpression.op_plus)
            {
                Object test1 = binary.getOperand1();
                Object test2 = binary.getOperand2();

                Object op1 = null;
                Object op2 = null;
                if (test1 instanceof IASTIdExpression && instanceRegistry.containsKey(((IASTIdExpression) test1).getName().toString()))
                {
                    op1 = instanceRegistry.containsKey(((IASTIdExpression) test1).getName().toString());
                }
                else if (test1 instanceof IASTLiteralExpression)
                {
                    op1 = parseLiteral(test1.toString(), Integer.class);
                }
                if (test2 instanceof IASTIdExpression && instanceRegistry.containsKey(((IASTIdExpression) test2).getName().toString()))
                {
                    op2 = instanceRegistry.get(((IASTIdExpression) test2).getName().toString());
                }
                else if (test2 instanceof IASTLiteralExpression)
                {
                    op2 = parseLiteral(test2.toString(), Integer.class);
                }
                if (op1 instanceof Integer && op2 instanceof Integer)
                {
                    Integer int1 = (Integer) op1;
                    Integer int2 = (Integer) op2;
                    initLiteral(thisObject, String.valueOf(int1 + int2), parentEObject, feature, addToMap, index);
                }
            }
        }

        private void handleInitializerList(IASTInitializerClause clause, Object thisObject)
        {
            if (thisObject instanceof EObject)
            {
                EObject thisListEOBject = (EObject) thisObject;
                // If this element is a Tab class, forward clause through its "element" feature
                if (tabClasses.contains(thisListEOBject.eClass()))
                {
                    EStructuralFeature elementFeature = thisListEOBject.eClass().getEAllStructuralFeatures().get(0);
                    Object element = thisListEOBject.eGet(elementFeature);
                    if (element instanceof List< ? >)
                    {
                        List<Object> elementValueList = (List<Object>) element;
                        IASTInitializerClause[] subClauses = ((IASTInitializerList) clause).getClauses();
                        // elementValueList can be empty or incomplete if an array declaration does not declare its size
                        // within the array modifier (ie, within the brackets A array[N]). An unknown
                        // cardinality causes the first emptyInit to not generate any children for this
                        // element. if this is the case, generate each of the children in turn
                        boolean isInitialized = elementValueList.size() >= subClauses.length;
                        for (int i = 0; i < subClauses.length; i++)
                        {
                            Object subValue = null;
                            if (isInitialized)
                            {
                                subValue = elementValueList.get(i);
                            }
                            else
                            {
                                subValue = emptyInit(thisListEOBject, thisListEOBject.eClass(), elementFeature.getEType());
                                elementValueList.add(subValue);
                            }
                            IASTInitializerClause subClause = subClauses[i];
                            recursiveProcessClause(subClause, subValue, thisListEOBject, elementFeature, false, i);
                        }
                    }
                }
                else
                {
                    IASTInitializerClause[] subClauses = ((IASTInitializerList) clause).getClauses();
                    for (int i = 0; i < subClauses.length; i++)
                    {
                        IASTInitializerClause subClause = subClauses[i];
                        EStructuralFeature subFeature = thisListEOBject.eClass().getEAllStructuralFeatures().get(i);
                        Object subValue = thisListEOBject.eGet(subFeature);
                        recursiveProcessClause(subClause, subValue, thisListEOBject, subFeature, false, 0);
                    }
                }
            }
        }

        private void handleIdExpressionClause(IASTInitializerClause clause, Object thisObject, EObject parentEObject, EStructuralFeature feature)
        {
            String instanceName = ((IASTIdExpression) clause).getName().toString();
            Object instance = instanceRegistry.get(instanceName);
            if (instance != null)
            {
                if (instance instanceof EEnumLiteral || (feature instanceof EReference && !((EReference) feature).isContainment()))
                {
                    parentEObject.eSet(feature, instance);
                }
                else
                {
                    // Copy value from instance to thisObject
                    copyStruct(instance, thisObject);
                }
            }
        }

        // find the corresponding feature name then loop back into recursive process clause
        private void handleDesignedInitializer(IASTInitializerClause clause, Object thisObject, EObject parentEObject)
        {
            ICASTDesignatedInitializer dInit = (ICASTDesignatedInitializer) clause;
            ICASTDesignator[] designators = dInit.getDesignators();
            String featureName = null;
            if (designators != null && designators.length > 0)
            {
                IASTNode[] children = designators[0].getChildren();
                for (IASTNode childNode : children)
                {
                    if (childNode instanceof IASTName)
                    {
                        featureName = childNode.toString();
                        EStructuralFeature feature = parentEObject.eClass().getEStructuralFeature(featureName);
                        if (feature != null)
                        {
                            recursiveProcessClause(dInit.getOperand(), parentEObject.eGet(feature), parentEObject, feature, false, 0);
                        }
                    }
                }
            }
        }

        private void handleLiteralExpressionClause(IASTInitializerClause clause, Object thisObject, EObject parentEObject, EStructuralFeature feature, boolean addToMap, int index)
        {
            String litValue = String.copyValueOf(((IASTLiteralExpression) clause).getValue());
            initLiteral(thisObject, litValue, parentEObject, feature, addToMap, index);
        }

        private void copyStruct(Object source, Object target)
        {
            if (source instanceof EObject && target instanceof EObject && ((EObject) source).eClass().equals(((EObject) target).eClass()))
            {
                EObject eSource = (EObject) source;
                EObject eTarget = (EObject) target;
                EClass eClass = eSource.eClass();

                for (EStructuralFeature feature : eClass.getEAllStructuralFeatures())
                {
                    Object eSourceChild = eSource.eGet(feature);
                    Object eTargetChild = eTarget.eGet(feature);

                    if (eSourceChild != null && eTargetChild != null && eSourceChild.getClass().equals(eTargetChild.getClass()) && !(eSourceChild instanceof EObject))
                    {
                        // If primitive, use the initLiteral function to copy value
                        // this is safe to call without checking the exact type of source and target
                        // as this check is done within initLiteral
                        initLiteral(eTargetChild, eSourceChild.toString(), eTarget, feature, false, 0);
                    }
                    else
                    {
                        // If eSourceChild / eTargetChild are not structs this call will do nothing
                        copyStruct(eSourceChild, eTargetChild);
                    }
                }
            }
            else if (target == null)
            {
                source = null;
            }
        }

        private void initLiteral(Object thisObject, String litValue, EObject parent, EStructuralFeature feature, boolean addToMap, int index)
        {
            Class expectedClass = thisObject.getClass();
            Object replacementLiteral = parseLiteral(litValue, expectedClass);
            if (replacementLiteral != null)
            {
                if (addToMap)
                {
                    instanceRegistry.put(declaredName, replacementLiteral);
                }

                // Literal initializations require us to replace the entire object rather
                // than simply updating the value
                if (feature.isMany())
                {
                    List<Object> list = (List<Object>) parent.eGet(feature);
                    if (index != -1)
                    {
                        list.remove(index);
                        list.add(index, replacementLiteral);
                    }
                }
                else
                {
                    parent.eSet(feature, replacementLiteral);
                }
            }
        }

        private Object emptyInit(EObject parent, EClass eClass, EClassifier classifier)
        {
            Object result = null;
            // e.g: {int 1;} OR { Int18 a; }
            if (classifier instanceof EDataType)
            {
            	//TODO figure out why returning "" as emptyInitString for character types causes the factory
            	//to raise an exception
            	if(!Character.class.equals(classifier.getInstanceClass())) {
            		String emptyInitString = getEmptyInitString((EDataType) classifier);
            		result = factory.createFromString((EDataType) classifier, emptyInitString);
            	}
            	else{
            		result = ' ';
            	}
            }
            // e.g: { ComplexType b; }
            else if (classifier instanceof EClass)
            {
                result = factory.create((EClass) classifier);
                EClass classifierEClass = (EClass) classifier;
                EObject resultEObject = (EObject) result;
                for (EStructuralFeature feature : classifierEClass.getEAllStructuralFeatures())
                {
                    if (feature instanceof EReference && !((EReference) feature).isContainment())
                    {
                        continue;
                    }

                    if (feature.isMany())
                    {
                        Object list = resultEObject.eGet(feature);
                        if (list instanceof List)
                        {
                            for (int i = 0; i < feature.getUpperBound(); i++)
                            {
                                Object child = emptyInit(resultEObject, classifierEClass, feature.getEType());
                                ((List) list).add(child);
                            }
                        }
                    }
                    else
                    {
                        Object child = emptyInit(resultEObject, classifierEClass, feature.getEType());
                        resultEObject.eSet(feature, child);
                    }
                }
            }
            return result;
        }
    }

    public void reset()
    {
        instance = null;
        initQueue.clear();
        typeRegistry.clear();
        instanceRegistry.clear();
        tabClasses.clear();
    }
}
