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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.topcased.checktool.utils.CUtilsQueueManager.TypeDefType;

public class CUtils
{
    protected static CUtils instance;

    public static CUtils getInstance()
    {
        if (instance == null)
        {
            instance = new CUtils();
        }
        return instance;
    }

    // The visitor in charge of visiting the C file and initializing the metamodel and model instances
    protected CUtilASTVisitor visitor;

    protected Resource parseCFile(IFile file, ResourceSet resSet)
    {
        try
        {
            // Create translation unit for file
            IPath path = file.getLocation();
            String lastSegment = path.removeFileExtension().lastSegment();
            ITranslationUnit unit = CoreModelUtil.findTranslationUnit(file);
            IASTTranslationUnit ast = unit.getAST();

            // Create Ecore Package containing the TU's metamodel and launch visit of C's AST model
            EPackage metaModelpackage = EcoreFactory.eINSTANCE.createEPackage();
            visitFile(metaModelpackage, lastSegment, ast, file.getProject());

//             testMetaModel(metaModelpackage, file);

            // finally, register these metamodels
            resSet.getPackageRegistry().put(metaModelpackage.getNsURI(), metaModelpackage);
            EPackage.Registry.INSTANCE.put(metaModelpackage.getNsURI(), metaModelpackage);

            // add metamodel and model to the same resource so they can be seen in the results view
            IPath resultPath = new Path(file.getLocation().removeFileExtension().toString() + ".xmi");
            Resource resource = resSet.createResource(URI.createFileURI(resultPath.toString()));
            resource.getContents().add(visitor.getInstanceRootEObject());
            resource.getContents().add(metaModelpackage);

            // createInstanceFile(visitor.getInstanceRootEObject(), metaModelpackage, file, resSet);
            return resource;
        }
        catch (Exception e)
        {
            //In case of failure, do not stop execution. This can happen if c file is bad
            e.printStackTrace();
        }
        return null;
    }

    protected void visitFile(EPackage pack, String lastSegment, IASTTranslationUnit ast, IProject iProject)
    {
        pack.setName(lastSegment);
        pack.setNsPrefix(lastSegment);
        pack.setNsURI("http://www.topcased.org/checktool/" + lastSegment);
        EClass clazz = EcoreFactory.eINSTANCE.createEClass();
        clazz.setName(lastSegment);
        visitor = new CUtilASTVisitor(pack, clazz);
        ast.accept(visitor);
    }

    private void testMetaModel(EPackage pack, IFile file)
    {
        IPath resultPath = file.getLocation().removeFileExtension().addFileExtension("ecore");
        ResourceSet resSet = new ResourceSetImpl();
        Resource resource = resSet.createResource(URI.createFileURI(resultPath.toString()));
        resource.getContents().add(pack);
        try
        {
            resource.save(Collections.emptyMap());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private IFile createInstanceFile(EObject rootEObject, EPackage metaModelpackage, IFile file, ResourceSet resSet)
    {
        IPath resultPath = new Path(file.getLocation().removeFileExtension().toString() + ".xmi");
        Resource resource = resSet.createResource(URI.createFileURI(resultPath.toString()));
        resource.getContents().add(rootEObject);
        try
        {
            Map<String, Boolean> options = new HashMap<String, Boolean>();
            options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
            resource.save(options);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(resultPath);
    }

    public class CUtilASTVisitor extends ASTVisitor
    {
        private final EPackage pack;

        private final Map<String, EClassifier> types;

        private final EClass clazz;

        // The base "File" object for the instance model
        private EObject instanceRootEObject;

        public CUtilASTVisitor(EPackage pack, EClass clazz)
        {
            super(true);
            this.pack = pack;
            this.clazz = clazz;
            types = new HashMap<String, EClassifier>();
            types.put(clazz.getName(), clazz);
            CUtilsQueueManager.getInstance().reset();
        }

        public Map<String, EClassifier> getTypes()
        {
            return types;
        }

        @Override
        public int visit(IASTTranslationUnit tu)
        {
            return super.visit(tu);
        }

        @Override
        public int leave(IASTTranslationUnit tu)
        {
            CUtilsQueueManager.getInstance().finishMetamodel(types);
            for (EClassifier classifier : types.values())
            {
                pack.getEClassifiers().add(classifier);
            }
            setInstanceRootEObject(pack.getEFactoryInstance().create(clazz));
            CUtilsQueueManager.getInstance().finishModel(clazz.getName(), getInstanceRootEObject(), pack);
            return super.leave(tu);
        }

        @Override
        public int visit(IASTDeclaration declaration)
        {
            IASTDeclSpecifier specifier;
            IASTNode[] children = declaration.getChildren();
            for (int i = 0; i < children.length; i++)
            {
                final IASTNode child = children[i];
                if (child instanceof IASTDeclSpecifier)
                {
                    specifier = (IASTDeclSpecifier) child;
                    // if defining an enumeration, struct or type
                    if (specifier.getStorageClass() == IASTDeclSpecifier.sc_typedef)
                    {
                        TypeDefType type = TypeDefType.getType(specifier);
                        CUtilsQueueManager.getInstance().addTypeDefPPA(declaration, type);
                    }
                    // Other declarations
                    else
                    {
                        CUtilsQueueManager.getInstance().addDeclarationPPA(clazz.getName(), declaration);
                    }
                }
            }
            return ASTVisitor.PROCESS_SKIP;
        }

        @Override
        public int visit(IASTInitializer initializer)
        {
            return super.visit(initializer);
        }

        public EObject getInstanceRootEObject()
        {
            return instanceRootEObject;
        }

        public void setInstanceRootEObject(EObject instanceRootEObject)
        {
            this.instanceRootEObject = instanceRootEObject;
        }

    }
}
