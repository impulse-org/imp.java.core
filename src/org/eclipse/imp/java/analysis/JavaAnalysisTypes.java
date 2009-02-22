package org.eclipse.imp.java.analysis;

import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;

public class JavaAnalysisTypes {
    public static final TypeFactory tf= TypeFactory.getInstance();
    public static final TypeStore javaAnalysisTypes = new TypeStore();

    /**
     * The type of a complete hierarchy, including both interfaces and classes
     */
    public static final Type JavaTypeType= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.type", tf.stringType());

    /**
     * The type of a hierarchy including only interfaces
     */
    public static final Type JavaInterfaceType= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.interfaceType", JavaTypeType);

    /**
     * The type of a hierarchy including only classes
     */
    public static final Type JavaClassType= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.classType", JavaTypeType);

    public static final Type JavaTypeHierarchyType= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.typeHierarchy", tf.relType(tf.tupleType(JavaTypeType, JavaTypeType)));
    public static final Type JavaInterfaceHierarchyType= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.interfaceHierarchy", tf.relType(tf.tupleType(JavaInterfaceType, JavaInterfaceType)));
    public static final Type JavaClassHierarchyType= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.classHierarchy", tf.relType(tf.tupleType(JavaClassType, JavaClassType)));

    //  public static final aliasType JavaCallGraphType= tf.aliasType("org.eclipse.imp.java.simpleCallGraphType", tf.relTypeOf(JavaSimpleMethodType, JavaSimpleMethodType));

    public static final Type EXTENDS_HIERARCHY= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.extendsHierarchy", JavaTypeHierarchyType);

    public static final Type IMPLEMENTS_HIERARCHY= tf.aliasType(javaAnalysisTypes, "org.eclipse.imp.java.implementsHierarchy", JavaTypeHierarchyType);
}
