package org.eclipse.imp.java.analysis;

import org.eclipse.imp.pdb.analysis.IFactGenerator;
import org.eclipse.imp.pdb.analysis.IFactGeneratorFactory;
import org.eclipse.imp.pdb.facts.impl.reference.ValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

public class JavaFactGeneratorFactory implements IFactGeneratorFactory {

    public IFactGenerator create(Type type) {
        if (type.equals(JavaAnalysisTypes.IMPLEMENTS_HIERARCHY) || type.equals(JavaAnalysisTypes.EXTENDS_HIERARCHY)) {
            return new JavaTypeExtractor();
        }
        throw new IllegalArgumentException("Java Fact Generator: don't know how to produce fact of type " + type);
    }

    public String getName() {
        return "Java Fact Generator Factory";
    }
    
    public void declareTypes(TypeFactory factory) {
        // this initializes the static values in JavaAnalysisTypes
        JavaAnalysisTypes.JavaClassHierarchyType.getTypeDescriptor(ValueFactory.getInstance());
    }
}
