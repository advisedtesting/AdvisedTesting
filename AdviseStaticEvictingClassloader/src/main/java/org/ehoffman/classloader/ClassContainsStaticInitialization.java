/*
 * Copyright © 2016, Saleforce.com, Inc
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

import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;

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
          && (!isFinal(access) && isEnumeration)) {
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
      if ("<clinit>".equals(name)) {
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
  }
  
}
