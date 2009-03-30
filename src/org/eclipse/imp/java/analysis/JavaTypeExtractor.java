package org.eclipse.imp.java.analysis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.java.JavaCorePlugin;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.model.ICompilationUnit;
import org.eclipse.imp.model.IPathEntry;
import org.eclipse.imp.model.ISourceEntity;
import org.eclipse.imp.model.ISourceFolder;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.IWorkspaceModel;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.analysis.AnalysisException;
import org.eclipse.imp.pdb.analysis.IFactGenerator;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.db.FactBase;
import org.eclipse.imp.pdb.facts.db.FactKey;
import org.eclipse.imp.pdb.facts.db.IFactContext;
import org.eclipse.imp.pdb.facts.db.context.ISourceEntityContext;
import org.eclipse.imp.pdb.facts.impl.reference.ValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.utils.StreamUtils;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.TypeNode;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.visit.NodeVisitor;

public class JavaTypeExtractor implements IFactGenerator {
    private final ValueFactory vf= ValueFactory.getInstance();

    private IParseController fParseController;

    private IRelationWriter fExtendsRW;

    private IRelationWriter fImplementsRW;

    public void generate(FactBase factBase, Type type, IFactContext context) throws AnalysisException {
        if (!type.equals(JavaAnalysisTypes.IMPLEMENTS_HIERARCHY) && !type.equals(JavaAnalysisTypes.EXTENDS_HIERARCHY)) {
            throw new AnalysisException("Java fact generator asked for unknown fact type: " + type);
        }

        ISourceEntityContext sec= (ISourceEntityContext) context;
        ISourceEntity srcEntity= sec.getEntity();
        final ISourceProject srcProject= (ISourceProject) srcEntity.getAncestor(ISourceProject.class);

        Language javaLang= LanguageRegistry.findLanguage(JavaCorePlugin.getInstance().getLanguageID());
        fParseController= ServiceFactory.getInstance().getParseController(javaLang);
        List<IPath> scanPath= getEntitiesForContext(srcEntity);
        IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
        IPath wsPath= wsRoot.getLocation();

        fExtendsRW= JavaAnalysisTypes.JavaTypeHierarchyType.writer(vf);
        fImplementsRW= JavaAnalysisTypes.JavaTypeHierarchyType.writer(vf);
        for(IPath path: scanPath) {
            if (wsPath.isPrefixOf(path)) {
                IResource resource= wsRoot.findMember(path.removeFirstSegments(wsPath.segmentCount()));
                try {
                    resource.accept(new IResourceVisitor() {
                        public boolean visit(IResource resource) throws CoreException {
                            if (resource instanceof IFile) {
                                IFile file= (IFile) resource;
                                if ("jar".equals(file.getFileExtension())) {
                                    try {
                                        processJarFile(file.getLocation(), srcProject);
                                    } catch (IOException e) {
                                        throw wrapException(e);
                                    }
                                } else if ("java".equals(file.getFileExtension())) {
                                    processSourceFile(file);
                                } else if ("class".equals(file.getFileExtension())) {
                                    processClassFile(file);
                                }
                                return false;
                            }
                            return true;
                        }
                    });
                } catch (CoreException e) {
                    throw new AnalysisException("Exception encountered during type hierarchy extraction: " + e.getMessage(), e);
                }
            } else { // it's outside the workspace
                if ("jar".equals(path.getFileExtension())) {
                    try {
                        processJarFile(path, srcProject);
                    } catch (IOException e) {
                        JavaCorePlugin.getInstance().logException("Error processing jar file " + path, e);
                    }
                }
            }
        }
        fExtendsRW.done();
        fImplementsRW.done();
        factBase.defineFact(new FactKey(JavaAnalysisTypes.EXTENDS_HIERARCHY, context), fExtendsRW.done());
        factBase.defineFact(new FactKey(JavaAnalysisTypes.IMPLEMENTS_HIERARCHY, context), fImplementsRW.done());
    }

    private void processClassFile(IFile file) throws CoreException {
        processClassFile(file.getLocation(), file.getContents());
    }

    private void processClassFile(IPath path, InputStream contents) {
        System.err.println("Class files not yet supported.");
//        throw new UnsupportedOperationException("Can't handle class files, knuckle-head!");
    }

    private void processSourceFile(IFile file) throws CoreException {
        try {
            processSourceFile(file.getLocation(), file.getContents(), ModelFactory.open(file.getProject()));
        } catch (ModelException e) {
            throw wrapException(e);
        }
    }

    private CoreException wrapException(Exception e) {
        return new CoreException(new Status(IStatus.ERROR, JavaCorePlugin.kPluginID, 0, e.getMessage(), e));
    }

    private void processSourceFile(IPath path, InputStream is, ISourceProject srcProject) {
        String contents= StreamUtils.readStreamContents(is);
        IMessageHandler mh= new IMessageHandler() {
            public void startMessageGroup(String groupName) { }
            public void endMessageGroup() { }
            public void handleSimpleMessage(String msg, int startOffset, int endOffset, int startCol, int endCol, int startLine, int endLine) {
                // hasError[0]= true;
            }
            public void clearMessages() { }
        };
        fParseController.initialize(path.removeFirstSegments(srcProject.getRawProject().getLocation().segmentCount()), srcProject, mh);
        SourceFile astRoot= (SourceFile) fParseController.parse(contents, new NullProgressMonitor());

        extractTypeRelations(astRoot);
    }

    private void extractTypeRelations(SourceFile astRoot) {
        if (astRoot == null) {
            System.out.println("Hey, no tree");
            return;
        }
        astRoot.visit(new NodeVisitor() {
            @Override
            public NodeVisitor enter(Node n) {
                if (n instanceof ClassDecl) {
                    ClassDecl cd= (ClassDecl) n;
                    ParsedClassType cdType= cd.type();
                    TypeNode superTypeNode = cd.superClass();
                    ClassType sc;
                    
                    if (superTypeNode != null) {
                        polyglot.types.Type superType= superTypeNode.type();
                        if (superType instanceof ClassType) {
                        	sc = (ClassType) superType; // safe b/c supertype of a ClassType is always a ClassType
                        } else {
                        	System.out.println("Encountered unknown type " + superType + " as super type of " + cd.name());
                        	sc= null;
                        }
                    }
                    else {
                        sc = cdType.typeSystem().Object();
                    }
                  
                    fExtendsRW.insert(vf.tuple(typeNameFor(cdType), typeNameFor(sc)));
                    
                    List<TypeNode> intfs= cd.interfaces();
                    for (TypeNode tn : intfs) {
						if (cdType.isClass()) {
							fImplementsRW.insert(vf.tuple(typeNameFor(cdType),
									typeNameFor((ClassType) tn.type())));
						} else {
							fExtendsRW.insert(vf.tuple(typeNameFor(cdType),
									typeNameFor((ClassType) tn.type())));
						}
					}
                }
                return this;
            }
            private IValue typeNameFor(ClassType cdType) {
            	if (cdType != null) {
                  return JavaAnalysisTypes.JavaClassType.make(vf, cdType.fullName());
            	}
            	else {
            		return JavaAnalysisTypes.JavaClassType.make(vf, "<unknown>");
            	}
            }
        });
    }

    private void processJarFile(IPath path, ISourceProject project) throws IOException {
        JarFile jarFile= new JarFile(new File(path.toOSString()));
        Enumeration<JarEntry> entryEnum= jarFile.entries();

        while (entryEnum.hasMoreElements()) {
            JarEntry jarEntry= entryEnum.nextElement();
            String entryName= jarEntry.getName();
            IPath entryPath= path.append(jarEntry.getName());

            if (entryName.endsWith(".class")) {
                processClassFile(entryPath, jarFile.getInputStream(jarEntry));
            } else if (entryName.endsWith(".java")) {
                // TODO: this triggers a FileNotFoundException in polyglot.frontend.FileSource:30
//                processSourceFile(entryPath, jarFile.getInputStream(jarEntry), project);
            }
        }
    }

    /**
     * @return a list of folders and jars to be scanned for source and class
     *         files
     */
    private List<IPath> getEntitiesForContext(ISourceEntity entity) {
        if (entity instanceof ISourceFolder) {
            return Collections.singletonList(entity.getResource().getLocation());
        }
        if (entity instanceof ISourceProject) {
            List<IPath> result= new ArrayList<IPath>();
            ISourceProject sp= (ISourceProject) entity;
            IPath wsPath= ResourcesPlugin.getWorkspace().getRoot().getLocation();
            List<IPathEntry> buildPath= sp.getBuildPath();
            for(IPathEntry entry : buildPath) {
                if (entry.getEntryType() == IPathEntry.PathEntryType.ARCHIVE) {
                    result.add(entry.getPath());
                } else if (entry.getEntryType() == IPathEntry.PathEntryType.PROJECT) {
                    throw new UnsupportedOperationException("Don't handle cross-project dependencies");
                } else if (entry.getEntryType() == IPathEntry.PathEntryType.SOURCE_FOLDER) {
                    if (entry.getPath().isAbsolute()) {
                        result.add(wsPath.append(entry.getPath()));
                    } else {
                        result.add(sp.getRawProject().getLocation().append(entry.getPath()));
                    }
                } else if (entry.getEntryType() == IPathEntry.PathEntryType.CONTAINER) {
                    throw new UnsupportedOperationException("Don't handle container classpath entries");
                }
            }
            return result;
        }
        if (entity instanceof ICompilationUnit) {
            return Collections.singletonList(entity.getResource().getLocation());
        }
        if (entity instanceof IWorkspaceModel) {
            throw new UnsupportedOperationException("Can't handle workspace context");
        }
        return null;
    }

   
}
