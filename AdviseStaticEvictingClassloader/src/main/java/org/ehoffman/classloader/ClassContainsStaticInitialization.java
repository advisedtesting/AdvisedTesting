/*
 * The MIT License
 * Copyright © 2016 Rex Hoffman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ehoffman.classloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.Handle;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.TypePath;

public class ClassContainsStaticInitialization implements Function<String, List<String>>, Predicate<String> {

  private int getVersionOpcode() {
    try {
      if (Opcodes.class.getField("ASM6") != null) {
        return Opcodes.ASM6;
      }
    } catch (NoSuchFieldException | SecurityException e1) {
      try {
        if (Opcodes.class.getField("ASM5") != null) {
          return Opcodes.ASM5;
        }
      } catch (NoSuchFieldException | SecurityException e2) {
        return Opcodes.ASM4;
      }
    }
    return Opcodes.ASM4;
  }
  
  private final int versionOpcode;
  
  public ClassContainsStaticInitialization() {
    this.versionOpcode = getVersionOpcode();
  }
  
  public boolean test(String className) {
    ClassReader reader;
    try {
      reader = new ClassReader(className);
      UnsafeClassVistor visitor = new UnsafeClassVistor(versionOpcode);
      reader.accept(visitor, 0);
      return visitor.shouldEvict();
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Class is not readable " + className.replace('/', '.'), ioe);
    }
  }
  
  public boolean test(byte[] bytes) {
    ClassReader reader;
    reader = new ClassReader(bytes);
    UnsafeClassVistor visitor = new UnsafeClassVistor(versionOpcode);
    reader.accept(visitor, 0);
    return visitor.shouldEvict();
  }

  @Override
  public List<String> apply(String className) {
    ClassReader reader;
    try {
      reader = new ClassReader(className);
      UnsafeClassVistor visitor = new UnsafeClassVistor(versionOpcode, true);
      reader.accept(visitor, 0);
      return visitor.getErrors();
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Class is not readable " + className.replace('/', '.'), ioe);
    }
  }
  
  public List<String> apply(byte[] bytes) {
    ClassReader reader;
    reader = new ClassReader(bytes);
    UnsafeClassVistor visitor = new UnsafeClassVistor(versionOpcode, true);
    reader.accept(visitor, 0);
    return visitor.getErrors();
  }
 
  /**
   * Not thread safe.
   * 
   * @author rex
   */
  private static class UnsafeClassVistor extends ClassVisitor {

    private final boolean allowAssertions;
    
    private final boolean captureErrors;
    
    private boolean shouldEvict = false;
    
    private String className;
    
    private boolean isEnumeration = false; //enumerations can not avoid static member variables -- just make sure they are final.
    
    private final List<String> errors;
    
    public UnsafeClassVistor(int api, boolean captureErrors, boolean allowAssertionKeyWord) {
      super(api);
      this.allowAssertions = allowAssertionKeyWord;
      this.captureErrors = captureErrors;
      if (captureErrors) {
        errors = new ArrayList<>();
      } else {
        errors = null;
      }
    }
    
    public UnsafeClassVistor(int api, boolean captureErrors) {
      this(api, captureErrors, true);
    }
    
    public UnsafeClassVistor(int api) {
      this(api, false);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      className = name;
      isEnumeration = isEnum(access);
    }    
    
    private boolean isStaticFinalLiteral(int access, Object value) {
      return isStatic(access) && isFinal(access) && value != null;
    }
    
    private boolean isStaticFinalEnumeration(int access) {
      return isStatic(access) && isFinal(access) && isEnumeration;
    }
    
    private boolean isAssertionSupport(String name) {
      return "$assertionsDisabled".equals(name) && allowAssertions;
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      if (isStatic(access) 
          && !isSynthetic(access)
          && !(isStaticFinalEnumeration(access)
               || isStaticFinalLiteral(access, value)
               || isAssertionSupport(name))) {
        shouldEvict = true;
        if (captureErrors) {
          addError("Disallowed static field with name \"" + name + "\"");
        }
      }
      return super.visitField(access, name, desc, signature, value);
    }
    
    public boolean isStatic(int access) {
      return ((access & Opcodes.ACC_STATIC) != 0);
    }

    public boolean isFinal(int access) {
      return ((access & Opcodes.ACC_FINAL) != 0);
    }
    
    public boolean isEnum(int access) {
      return ((access & Opcodes.ACC_ENUM) != 0);
    }
    
    public boolean isSynthetic(int access) {
      return ((access & Opcodes.ACC_SYNTHETIC) != 0);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, 
                              String desc, String signature, String[] exceptions) {
      if (!isEnumeration && "<clinit>".equals(name)) {
        if (allowAssertions) {
          return new UnsafeStaticInitVistor(this.api, this);
        }
        shouldEvict = true;
      }
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
    
    public boolean shouldEvict() {
      return shouldEvict;
    }
    
    public List<String> getErrors() {
      return errors;
    }
    
    public void addError(String error) {
      if (errors != null) {
        this.errors.add(error + " on class: " + className.replace('/', '.'));
      }
    }
    
    public void evict() {
      this.shouldEvict = true;
    }
    
  }
  
  /**
   * <p>
   * This methodVisitor evicts any method who's only operations
   * are anything other than setting the class's static
   * $assertionsDisabled with {@link Class#desiredAssertionStatus}'s
   * result.
   * </p>
   * <p>
   * If a class uses the assert keyword this static member variable
   * is constructed and set in this way.
   * </p>
   * <p>
   * Should only be used to validate &lt;cinit&gt; methods.
   * </p>
   * @author rex
   */
  public static class UnsafeStaticInitVistor extends MethodVisitor {

    private boolean shouldEvict = false;
    
    private final UnsafeClassVistor visitor;
    
    public UnsafeStaticInitVistor(int api, UnsafeClassVistor visitor) {
      super(api);
      this.visitor = visitor;
    }
    

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      shouldEvict = shouldEvict 
              || opcode != Opcodes.PUTSTATIC
              || !"$assertionsDisabled".equals(name);
      super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      shouldEvict = true;
      super.visitLocalVariable(name, desc, signature, start, end, index);
    }
    
    @Override
    public void visitEnd() {
      if (shouldEvict) {
        visitor.evict();
        visitor.addError("Disallowed <cinit> method (does more than enable the assert keyword)");
      }
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
      shouldEvict = true;
      super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
      // TODO Auto-generated method stub
      shouldEvict = shouldEvict 
          || opcode != Opcodes.INVOKEVIRTUAL
          || !"java/lang/Class".equals(owner)
          || !"desiredAssertionStatus".equals(name)
          || !"()Z".equals(desc);
      super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      shouldEvict = true;
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
      shouldEvict = true;
      super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      shouldEvict = true;
      super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      shouldEvict = true;
      super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      shouldEvict = true;
      super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
      shouldEvict = true;
      return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index,
            String desc, boolean visible) {
      shouldEvict = true;
      return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      super.visitLineNumber(line, start);
    }
    
  }

}
