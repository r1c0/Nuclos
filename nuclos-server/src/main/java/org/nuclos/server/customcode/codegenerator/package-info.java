/**
 * Codegeneration and (low level) code instrumentation (i.e. AOP) support for 
 * Nuclos.
 * <p>
 * Good bytecode engineering start points:
 * <ul>
 *   <li>{@link http://www.fh-wedel.de/~si/seminare/ws07/Ausarbeitung/05.jvm/jvm3.htm} Seminararbeit</li>
 *   <li>{@link http://www.murrayc.com/learning/java/java_classfileformat.shtml} class file format overview</li>
 *   <li>{@link http://www.ibm.com/developerworks/ibm/library/it-haggar_bytecode/} IBM article</li>
 * </ul>
 * </p><p>
 * Good ASM start points:
 * <ul>
 *   <li>{@link http://asm.ow2.org/doc/tutorial.html} ASM tutorial</li>
 *   <li>{@link http://asm.ow2.org/doc/faq.html} ASM FAQs</li>
 *   <li>{@link http://www.ibm.com/developerworks/java/library/j-cwt05125/index.html} IBM article about ASM</li>
 * </ul>
 * </p><p>
 * For a good start to actually see bytecode emitted by the javac compiler use:
 * <ul>
 *   <li>{@link http://andrei.gmxhome.de/bytecode/index.html} The bytecode outline eclipse plugin.</li>
 * </ul>
 * </p>
 * @author Thomas Pasch (package javadoc)
 */
package org.nuclos.server.customcode.codegenerator;
