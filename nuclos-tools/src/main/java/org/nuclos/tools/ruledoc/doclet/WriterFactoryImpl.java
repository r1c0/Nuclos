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
package org.nuclos.tools.ruledoc.doclet;

import com.sun.javadoc.*;
import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import com.sun.tools.doclets.internal.toolkit.util.VisibleMemberMap;

/**
 * The factory that returns HTML writers.
 *

 * @since 1.5
 */
public class WriterFactoryImpl implements WriterFactory {

	private static WriterFactoryImpl instance;

	private WriterFactoryImpl(ConfigurationImpl configuration) {
	}

	/**
	 * Return an instance of this factory.
	 *
	 * @return an instance of this factory.
	 */
	public static WriterFactoryImpl getInstance() {
		if (instance == null) {
			instance = new WriterFactoryImpl(ConfigurationImpl.getInstance());
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConstantsSummaryWriter getConstantsSummaryWriter() throws Exception {
		return null;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PackageSummaryWriter getPackageSummaryWriter(PackageDoc packageDoc,
			PackageDoc prevPkg, PackageDoc nextPkg) throws Exception {
		return new PackageWriterImpl(ConfigurationImpl.getInstance(), packageDoc,
				prevPkg, nextPkg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassWriter getClassWriter(ClassDoc classDoc, ClassDoc prevClass,
			ClassDoc nextClass, ClassTree classTree)
			throws Exception {
		return new ClassWriterImpl(classDoc, prevClass, nextClass, classTree);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnnotationTypeWriter getAnnotationTypeWriter(
			AnnotationTypeDoc annotationType, Type prevType, Type nextType)
			throws Exception {
		return new AnnotationTypeWriterImpl(annotationType, prevType, nextType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnnotationTypeOptionalMemberWriter
			getAnnotationTypeOptionalMemberWriter(
			AnnotationTypeWriter annotationTypeWriter) throws Exception {
		return null;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnnotationTypeRequiredMemberWriter
			getAnnotationTypeRequiredMemberWriter(AnnotationTypeWriter annotationTypeWriter) throws Exception {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EnumConstantWriter getEnumConstantWriter(ClassWriter classWriter)
			throws Exception {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FieldWriter getFieldWriter(ClassWriter classWriter)
			throws Exception {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodWriter getMethodWriter(ClassWriter classWriter)
			throws Exception {
		return new MethodWriterImpl((SubWriterHolderWriter) classWriter,
				classWriter.getClassDoc());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConstructorWriter getConstructorWriter(ClassWriter classWriter)
			throws Exception {
		return new ConstructorWriterImpl((SubWriterHolderWriter) classWriter,
				classWriter.getClassDoc());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemberSummaryWriter getMemberSummaryWriter(
			ClassWriter classWriter, int memberType)
			throws Exception {
		switch (memberType) {
			case VisibleMemberMap.METHODS:
				return (MethodWriterImpl) getMethodWriter(classWriter);
			case VisibleMemberMap.CONSTRUCTORS:
				return (ConstructorWriterImpl) getConstructorWriter(classWriter);

			case VisibleMemberMap.INNERCLASSES:
				return new NestedClassWriterImpl((SubWriterHolderWriter)
						classWriter, classWriter.getClassDoc());

			default:
				return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemberSummaryWriter getMemberSummaryWriter(
			AnnotationTypeWriter annotationTypeWriter, int memberType)
			throws Exception {
		switch (memberType) {

			default:
				return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SerializedFormWriter getSerializedFormWriter() throws Exception {
		return null;
	}
}
