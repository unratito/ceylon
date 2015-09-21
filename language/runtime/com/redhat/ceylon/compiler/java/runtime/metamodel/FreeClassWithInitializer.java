package com.redhat.ceylon.compiler.java.runtime.metamodel;

import ceylon.language.Sequential;
import ceylon.language.empty_;
import ceylon.language.meta.declaration.ConstructorDeclaration;

import com.redhat.ceylon.compiler.java.metadata.Ceylon;
import com.redhat.ceylon.compiler.java.metadata.Ignore;
import com.redhat.ceylon.compiler.java.metadata.TypeInfo;
import com.redhat.ceylon.compiler.java.runtime.model.TypeDescriptor;

@Ceylon(major = 8)
@com.redhat.ceylon.compiler.java.metadata.Class
public class FreeClassWithInitializer 
        extends FreeClass 
        implements ceylon.language.meta.declaration.ClassWithInitializerDeclaration {

    @Ignore
    public final static TypeDescriptor $TypeDescriptor$ = TypeDescriptor.klass(FreeClassWithInitializer.class);
    
    public FreeClassWithInitializer(com.redhat.ceylon.model.typechecker.model.Class declaration) {
        super(declaration);
    }

    @Override
    @Ignore
    public ceylon.language.meta.declaration.ClassWithInitializerDeclaration$impl 
    $ceylon$language$meta$declaration$ClassWithInitializerDeclaration$impl() {
        return new ceylon.language.meta.declaration.ClassWithInitializerDeclaration$impl(this);
    }
    
    @Ignore
    @Override
    public TypeDescriptor $getType$() {
        return $TypeDescriptor$;
    }
    
    @TypeInfo("ceylon.language::Empty")
    @Override
    public Sequential<? extends ConstructorDeclaration> constructorDeclarations() {
        return (Sequential)empty_.get_();
    }
    
    @TypeInfo("ceylon.language::Empty")
    @Override
    public <A extends java.lang.annotation.Annotation> Sequential<? extends ConstructorDeclaration> annotatedConstructorDeclarations(TypeDescriptor reified$Annotation) {
        return (Sequential)empty_.get_();
    }

}