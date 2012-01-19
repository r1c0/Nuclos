//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.

package org.nuclos.server.customcode.codegenerator;

import static org.objectweb.asm.Opcodes.*;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * An ASM ClassAdapter that logs each method body instruction line of the processed
 * class to log4j. This serves as poor men debugging facility for Nuclos rules.
 *
 * @author Thomas Pasch (javadoc)
 */
class ClassDebugAdapter extends ClassAdapter {

	private Map<String, String> varLookup = new HashMap<String, String>();
	private String className;
	private int headerCount;

	public ClassDebugAdapter(ClassVisitor cv, int headerCount) {
		super(cv);
		this.headerCount = headerCount;
	}

	public static byte[] weaveDebugInterceptors(byte[] bytes, int headerCount) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
		ClassAdapter ca = new ClassDebugAdapter(cw, headerCount);
		cr.accept(ca, 0);
		return cw.toByteArray();
	}

	@Override
	public void visit(final int version, final int access, final String name,
		final String signature, final String superName, final String[] interfaces) {
		this.className = name;
		cv.visit(version, access, name, signature, superName, interfaces);
		insertDebugOutMethod();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv;
		mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if(mv != null && !name.equals("<init>")) {
			mv = new MethodDebugAdapter(className, varLookup, mv, name, desc);
		}
		return mv;
	}

	@Override
	public void visitEnd() {
		insertDebugResolveMethod();
		cv.visitEnd();
	}

	private void insertDebugOutMethod() {
		/*
		Old code:
        private static void __out(String prefix, String varKey, Object o) {
           String var = __resolve(varKey);
           String value = "<null>";
           if(o != null)
              value = o.toString();
           __LOG.info(prefix + var + "=> " + value);
        }
        
        New code:
        private static void __out(String prefix, String varKey, Object o) {
        	String var = __resolve(varKey);
        	RuleDebugLoggerSingleton.getInstance().log(prefix, var, o);		
        }
        
        ASM code:
   L0
    ALOAD 1
    INVOKESTATIC org/nuclos/server/customcode/codegenerator/ClassDebugAdapter.__resolve(Ljava/lang/String;)Ljava/lang/String;
    ASTORE 3
   L1
    INVOKESTATIC org/nuclos/server/customcode/codegenerator/RuleDebugLoggerSingleton.getInstance()Lorg/nuclos/server/customcode/codegenerator/RuleDebugLoggerSingleton;
    ALOAD 0
    ALOAD 3
    ALOAD 2
    INVOKEVIRTUAL org/nuclos/server/customcode/codegenerator/RuleDebugLoggerSingleton.log(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V
   L2
    RETURN
   L3
    LOCALVARIABLE prefix Ljava/lang/String; L0 L3 0
    LOCALVARIABLE varKey Ljava/lang/String; L0 L3 1
    LOCALVARIABLE o Ljava/lang/Object; L0 L3 2
    LOCALVARIABLE var Ljava/lang/String; L1 L3 3
    MAXSTACK = 4
    MAXLOCALS = 4		 
		 */
		MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__out", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESTATIC, className, "__resolve", "(Ljava/lang/String;)Ljava/lang/String;");
		mv.visitVarInsn(ASTORE, 3);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitMethodInsn(INVOKESTATIC, "org/nuclos/server/customcode/codegenerator/RuleDebugLoggerSingleton", "getInstance", "()Lorg/nuclos/server/customcode/codegenerator/RuleDebugLoggerSingleton;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "org/nuclos/server/customcode/codegenerator/RuleDebugLoggerSingleton", "log", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitInsn(RETURN);
		mv.visitMaxs(4, 4);
		mv.visitEnd();
	}

	private void insertDebugResolveMethod() {
		/*
        private String __resolve(String methodAndIndex) {
           if(methodAndIndex.equals("method1"))
              return "var1";
           if(methodAndIndex.equals("method2"))
              return "var2";
           [ ... ]
           if(methodAndIndex.equals("methodX"))
              return "varX";
           return "-";
        }
		 */
		MethodVisitor mv = cv.visitMethod(ACC_PRIVATE + ACC_STATIC, "__resolve", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		for(String mapKey : varLookup.keySet()) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn(mapKey);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
			Label l1 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLdcInsn(varLookup.get(mapKey));
			mv.visitInsn(ARETURN);
			mv.visitLabel(l1);
			mv.visitFrame(F_SAME, 0, null, 0, null);
		}
		mv.visitLdcInsn("-");
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	private class MethodDebugAdapter extends MethodAdapter {
		private String className;
		private Map<String, String> varLookup = new HashMap<String, String>();
		private String methodName;
		private String desc;
		private int line;

		public MethodDebugAdapter(String className, Map<String, String> varLookup, MethodVisitor mv, String name, String desc) {
			super(mv);
			this.className = className;
			this.varLookup = varLookup;
			this.methodName = name;
			this.desc = desc;
		}

		@Override
		public void visitCode() {
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn(">>> " + className + "." + methodName + desc);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		}

		@Override
		public void visitInsn(final int opcode) {
			if(opcode == RETURN) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitLdcInsn("<<< " + className + "." + methodName + desc);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			}
			mv.visitInsn(opcode);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			mv.visitVarInsn(opcode, var);
			switch(opcode) {
			case ISTORE:
				insertDebugOutput(var, ILOAD, "java/lang/Integer", "(I)Ljava/lang/Integer;");
				break;
			case LSTORE:
				insertDebugOutput(var, LLOAD, "java/lang/Long", "(J)Ljava/lang/Long;");
				break;
			case FSTORE:
				insertDebugOutput(var, FLOAD, "java/lang/Float", "(F)Ljava/lang/Float;");
				break;
			case DSTORE:
				insertDebugOutput(var, DLOAD, "java/lang/Double", "(D)Ljava/lang/Double;");
				break;
			case ASTORE:
				insertDebugOutput(var, ALOAD, null, null);
				break;
			}
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			super.visitLocalVariable(name, desc, signature, start, end, index);
			if(index > 0)
				varLookup.put(methodName + index, getType(desc) + " " + name);
		}

		@Override
		public void visitLineNumber(int line, Label start) {
			super.visitLineNumber(line, start);
			this.line = line - headerCount;
		}

		@Override
		public void visitIincInsn(int var, int increment) {
			mv.visitIincInsn(var, increment);
			insertDebugOutput(var, ILOAD, "java/lang/Integer", "(I)Ljava/lang/Integer;");
		}

		private void insertDebugOutput(int var, int opcode, String valueClassName, String valueClassDesc) {
			// Method '__out' is now static, thus the stack size would change 
			// if we load the class instance here. (Thomas Pasch)
			// mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn("\t" + className + "." + methodName + desc + " [" + (this.line) + "] => ");
			mv.visitLdcInsn(methodName + var);
			mv.visitVarInsn(opcode, var);
			if(valueClassName != null && valueClassDesc != null)
				mv.visitMethodInsn(INVOKESTATIC, valueClassName, "valueOf", valueClassDesc);
			mv.visitMethodInsn(INVOKESTATIC, className, "__out", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");
		}

		private String getType(String desc) {
			if(desc == null)
				return null;
			if(desc.startsWith("L"))
				return desc.substring(desc.lastIndexOf("/") + 1, desc.length() - 1);
			else if(desc.equals("I"))
				return "int";
			else if(desc.equals("F"))
				return "float";
			else if(desc.equals("J"))
				return "long";
			else if(desc.equals("D"))
				return "double";
			else if(desc.equals("Z"))
				return "boolean";
			return desc;
		}
	}

}
