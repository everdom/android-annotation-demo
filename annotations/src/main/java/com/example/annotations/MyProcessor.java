package com.example.annotations;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import java.util.Set;

@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    //处理Element的工具类
    private Elements mElementUtils;
    //生成文件的工具
    private Filer mFiler;
    //日志信息的输出
    private Messager mMessager;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes(){
        Set<String> supportedType = new LinkedHashSet<String>();
        supportedType.add(MyAnnotation.class.getCanonicalName());
        return supportedType;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取MyAnnotation注解的类的集合
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(MyAnnotation.class);
        for (Element element : set){
            //这里往往要进行被注解类的匹配判断，看是否符合我们的要求
            // if(isVaild(element))...
            if(element.getKind() == ElementKind.CLASS){
                //TODO:写java文件
                TypeElement typeElement = (TypeElement) element;
                String qualifiedName = typeElement.getQualifiedName().toString();
                mMessager.printMessage(Diagnostic.Kind.NOTE,"qualifiedName:"+qualifiedName);
                brewJavaFile(typeElement);
            }else if(element.getKind() == ElementKind.METHOD){
                System.out.println("asfasfdas");
            }
        }

        return false;
    }

    private void brewJavaFile(TypeElement pElement){
        //sayHello 方法
        MyAnnotation myAnnotation = pElement.getAnnotation(MyAnnotation.class);
        MethodSpec methodSpec = MethodSpec.methodBuilder("sayHello")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(void.class)
                .addStatement("$T.out.println($S)",System.class,"Hello"+myAnnotation.value()).build();

        // class
        TypeSpec typeSpec = TypeSpec.classBuilder(pElement.getSimpleName().toString()+"$$HelloWorld").addModifiers(Modifier.PUBLIC,Modifier.FINAL).addMethod(methodSpec).build();
        // 获取包路径，把我们的生成的源码放置在与被注解类中同一个包路径中
        JavaFile javaFile = JavaFile.builder(mElementUtils.getPackageOf(pElement).getQualifiedName().toString(),typeSpec).build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
