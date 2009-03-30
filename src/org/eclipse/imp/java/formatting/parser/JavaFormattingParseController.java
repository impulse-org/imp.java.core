package org.eclipse.imp.java.formatting.parser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.java.formatting.parser.JavaParser.Ast;
import org.eclipse.imp.parser.ILexer;
import org.eclipse.imp.parser.IParser;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.MessageHandlerAdapter;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;

public class JavaFormattingParseController extends SimpleLPGParseController {
    private JavaLexer fLexer;
	private JavaParser fParser;

    public JavaFormattingParseController() {
        super("java");
    }

	@Override
	public ILexer getLexer() {
		return fLexer;
	}

	@Override
	public IParser getParser() {
		return fParser;
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
			fParser = new JavaParser(fLexer.getLexStream());
		}
		
		fParser.reset(fLexer.getLexStream());
		fParser.getParseStream().setMessageHandler(new MessageHandlerAdapter(handler));

		fLexer.lexer(my_monitor, fParser.getParseStream()); // Lex the char stream to
															// produce the token stream
		if (my_monitor.isCancelled())
			return fCurrentAst; // TODO fCurrentAst might (probably will) be
								// inconsistent wrt the lex stream now

		fCurrentAst = (Ast) fParser.parseExpression(my_monitor, 0);

		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseStatement(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseBlockStatements(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseClassBodyDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseTypeDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseImportDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseImportDeclarations(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parsePackageDeclaration(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseTypeName(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parseArgumentList(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parsePackageName(my_monitor, 0);
		}
		if (fCurrentAst == null) {
			fCurrentAst = (Ast) fParser.parser(my_monitor, 0);
		}
		cacheKeywordsOnce();

		Object result = fCurrentAst;
		return result;
	}
}
