/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.parser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.java.JavaCorePlugin;
import org.eclipse.imp.java.resolution.PolyglotNodeLocator;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.ILexer;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.IParser;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.FileSource;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.ParserlessJLExtensionInfo;
import polyglot.frontend.Source;
import polyglot.frontend.goals.Goal;
import polyglot.main.Options;
import polyglot.main.UsageError;
import polyglot.main.Version;
import polyglot.util.ErrorQueue;
import polyglot.util.SilentErrorQueue;

public class JavaParseController extends SimpleLPGParseController implements IParseController {
    private IJavaProject fJavaProject;
    private JavaParser fParser;
    private JavaLexer fLexer;
    private JavaExtensionInfo fExtInfo;

    public JavaParseController() {
        super(JavaCorePlugin.kLanguageID);
    }

    public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
        super.initialize(filePath, project, handler);
        if (project != null)
            this.fJavaProject= JavaCore.create(project.getRawProject());
    }

    public IParser getParser() {
        return fParser;
    }

    public ILexer getLexer() {
        return fLexer;
    }

    public ISourcePositionLocator getNodeLocator() {
        return new PolyglotNodeLocator(fLexer.getILexStream());
    }

    public ILanguageSyntaxProperties getSyntaxProperties() {
        return new JavaSyntaxProperties();
    }

    /**
     * @return a list of all project-relative CPE_SOURCE-type classpath entries.
     * @throws JavaModelException
     */
    private List<IPath> getProjectSrcPath() throws JavaModelException {
        List<IPath> srcPath= new ArrayList<IPath>();

        if (fJavaProject == null)
            return srcPath;

        IClasspathEntry[] classPath= fJavaProject.getResolvedClasspath(true);

        for(int i= 0; i < classPath.length; i++) {
            IClasspathEntry e= classPath[i];

            if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE)
                srcPath.add(e.getPath());
        }
        if (srcPath.size() == 0)
            srcPath.add(fJavaProject.getProject().getLocation());
        return srcPath;
    }

    private String pathListToPathString(List<IPath> pathList) {
        StringBuffer buff= new StringBuffer();

        for(Iterator<IPath> iter= pathList.iterator(); iter.hasNext();) {
            IPath path= iter.next();

            buff.append(fJavaProject.getProject().getWorkspace().getRoot().getLocation().append(path).toOSString());
            if (iter.hasNext())
                buff.append(';');
        }
        return buff.toString();
    }

    private void buildOptions(ExtensionInfo extInfo) {
        Options opts= extInfo.getOptions();
        Options.global= opts;
        try {
            List<IPath> projectSrcLoc= getProjectSrcPath();
            String projectSrcPath= pathListToPathString(projectSrcLoc);
            String classPathSpec= buildClassPathSpec();

            opts.parseCommandLine(new String[] { "-cp", classPathSpec, "-sourcepath", projectSrcPath }, new HashSet());
        } catch (UsageError e) {
            // We don't give any source files to parseCommandLine() above; they're furnished later.
            // Because of this, we'll get an innocuous UsageError exception, but everything will be
            // ok in spite of that.
            if (!e.getMessage().equals("must specify at least one source file")) {
                JavaCorePlugin.getInstance().writeErrorMsg(e.getMessage());
            }
        } catch (JavaModelException e) {
            JavaCorePlugin.getInstance().writeErrorMsg("Unable to obtain resolved class path: " + e.getMessage());
        }
        JavaCorePlugin.getInstance().maybeWriteInfoMsg("Source path = " + opts.source_path);
        JavaCorePlugin.getInstance().maybeWriteInfoMsg("Class path = " + opts.classpath);
    }

    private String buildClassPathSpec() {
        StringBuffer buff= new StringBuffer();

        try {
            IClasspathEntry[] classPath= (fJavaProject != null) ? fJavaProject.getResolvedClasspath(true) : new IClasspathEntry[0];

            for(int i= 0; i < classPath.length; i++) {
                IClasspathEntry entry= classPath[i];
                final String entryPath= entry.getPath().toOSString();

                if (i > 0)
                    buff.append(File.pathSeparatorChar);
                buff.append(entryPath);
            }
        } catch (JavaModelException e) {
            JavaCorePlugin.getInstance().writeErrorMsg("Error resolving class path: " + e.getMessage());
        }
        return buff.toString();
    }

    private final class JavaExtensionInfo extends ParserlessJLExtensionInfo {
        public String compilerName() {
            return "IMP Java Front-end";
        }

        public Version version() {
            return new Version() {
                @Override
                public String name() {
                    return "0.0.1";
                }

                @Override
                public int major() {
                    return 0;
                }

                @Override
                public int minor() {
                    return 0;
                }

                @Override
                public int patch_level() {
                    return 1;
                }
            };
        }

        public Goal getCompileGoal(Job job) {
            return scheduler.TypeChecked(job);
        }

        public Job getJob(Source source) {
            Collection jobs= scheduler.jobs();
            for(Iterator i= jobs.iterator(); i.hasNext();) {
                Job job= (Job) i.next();
                if (job.source() == source)
                    return job;
            }
            return null;
        }

        public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
            try {
                fLexer= new JavaLexer(source.path());
                fParser= new JavaParser(fLexer.getILexStream(), ts, nf, source, eq); // Create the parser
                fLexer.lexer(fParser.getIPrsStream());
                return fParser;
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e.getMessage());
            }
        }

        @Override
        public String defaultFileExtension() {
            return "java";
        }
    }

    public Object parse(String contents, boolean scanOnly, IProgressMonitor monitor) {
        FileSource fileSource= null;

        try {
            fExtInfo= new JavaExtensionInfo();
            ErrorQueue eq= new SilentErrorQueue(1000, "stdout");
            Compiler compiler= new Compiler(fExtInfo, eq);
            buildOptions(fExtInfo);
            fExtInfo.initCompiler(compiler);
            try {
                fileSource= new FileSource(new File(fProject.getRawProject().getLocation().append(fFilePath).toOSString()));
                List<Source> streams= new ArrayList<Source>();
                streams.add(fileSource);
                compiler.compile(streams);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        } finally {
            // RMF 8/2/2006 - retrieve the AST if there is one; some later phase of compilation
            // may fail, even though the AST is well-formed enough to provide an outline.
            if (fileSource != null)
                fCurrentAst= fExtInfo.getJob(fileSource).ast();
            cacheKeywordsOnce(); // better place/time to do this?
        }
        return fCurrentAst;
    }

    /*
     * For the management of associated problem-marker types
     */
    private static List<String> problemMarkerTypes= new ArrayList<String>();

    public List getProblemMarkerTypes() {
        return problemMarkerTypes;
    }

    public void addProblemMarkerType(String problemMarkerType) {
        problemMarkerTypes.add(problemMarkerType);
    }

    public void removeProblemMarkerType(String problemMarkerType) {
        problemMarkerTypes.remove(problemMarkerType);
    }
}
