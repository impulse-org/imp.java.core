package org.eclipse.imp.java.formatting.parser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.java.formatting.parser.JavaParser.Ast;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.MessageHandlerAdapter;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;

public class JavaFormattingParseController extends SimpleLPGParseController {
    public JavaFormattingParseController() {
        super("java");
    }

	public ISourcePositionLocator getSourcePositionLocator() {
		// Never called
		throw new UnsupportedOperationException("Someone called JavaFormattingParseController.getNodeLocator()?");
	}

	public ILanguageSyntaxProperties getSyntaxProperties() {
		// Never called
		throw new UnsupportedOperationException("Someone called JavaFormattingParseController.getSyntaxProperties()?");
	}

	public Object parse(String contents, IProgressMonitor monitor) {
		PMMonitor my_monitor = new PMMonitor(monitor);
		char[] contentsArray = contents.toCharArray();


		if (fLexer == null) {
			fLexer = new JavaLexer();
		}
		fLexer.reset(contentsArray, fFilePath.toOSString());
		
		if (fParser == null) {
			fParser = new JavaParser(fLexer.getILexStream());
		}
		
		fParser.reset(fLexer.getILexStream());
		fParser.getIPrsStream().setMessageHandler(new MessageHandlerAdapter(handler));

		fLexer.lexer(my_monitor, fParser.getIPrsStream()); // Lex the char stream to
															// produce the token stream
		if (my_monitor.isCancelled())
			return fCurrentAst; // TODO fCurrentAst might (probably will) be
								// inconsistent wrt the lex stream now

		JavaParser parser= (JavaParser) fParser;

		fCurrentAst = (Ast) parser.parseExpression(my_monitor, 0);

		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseStatement(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseBlockStatements(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseClassBodyDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseTypeDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseImportDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseImportDeclarations(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parsePackageDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseTypeName(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parseArgumentList(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) parser.parsePackageName(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parser(my_monitor, 0);
		}
		cacheKeywordsOnce();

		Object result = fCurrentAst;
		return result;
	}
}
