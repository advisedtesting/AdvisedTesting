/*
 * Copyright © 2016, Rex Hoffman
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ehoffman.classloader;

import java.io.IOException;
import java.util.function.Predicate;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Attribute;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.Handle;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.TypePath;

public class ClassContainsStaticInitialization implements Predicate<String> {

  public boolean test(String className) {
    ClassReader reader;
    try {
      reader = new ClassReader(className);
      UnsafeClassVistor visitor = new UnsafeClassVistor(Opcodes.ASM6);
      reader.accept(visitor, 0);
      return visitor.shouldEvict();
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Class is not readable " + className, ioe);
    }
  }
 
  private static class UnsafeClassVistor extends ClassVisitor {

    boolean allowAssertions = true;
    
    boolean shouldEvict = false;
    
    boolean isEnumeration = false; //enumerations can not avoid static member variables -- just make sure they are final.
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      isEnumeration = isEnum(access);
    }

    
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      if (isStatic(access) 
          && (!isFinal(access) || value == null)
          && (!isFinal(access) && isEnumeration)
          && (!"$assertionsDisabled".equals(name) && !allowAssertions)) {
        shouldEvict = true;
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
    
    public UnsafeClassVistor(int api) {
      super(api);
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
      }
    }

    @Override
    public void visitAttribute(Attribute attr) {
      super.visitAttribute(attr);
    }

    @Override
    public void visitCode() {
      super.visitCode();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
      super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitParameter(String name, int access) {
      // TODO Auto-generated method stub
      super.visitParameter(name, access);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
      // TODO Auto-generated method stub
      return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      // TODO Auto-generated method stub
      return super.visitParameterAnnotation(parameter, desc, visible);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
      super.visitFrame(type, numLocal, local, numStack, stack);
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
    public void visitLabel(Label label) {
      super.visitLabel(label);
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
