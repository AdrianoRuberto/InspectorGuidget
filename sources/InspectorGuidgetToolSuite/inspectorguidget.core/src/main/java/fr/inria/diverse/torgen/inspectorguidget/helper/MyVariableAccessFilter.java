/**
 * Copyright (C) 2006-2015 INRIA and contributors
 * Spoon - http://spoon.gforge.inria.fr/
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.inria.diverse.torgen.inspectorguidget.helper;

import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;

/**
 * This simple filter matches all the accesses to a given field.
 */
public class MyVariableAccessFilter<T extends CtVariableAccess<?>> implements Filter<T> {
	CtVariableReference<?> variable;

	/**
	 * Creates a new field access filter.
	 *
	 * @param variable
	 * 		the accessed variable
	 */
	public MyVariableAccessFilter(CtVariableReference<?> variable) {
		this.variable = variable;
	}

	@Override
	public boolean matches(T variableAccess) {
		CtVariableReference<?> varAc = variableAccess.getVariable();

		if(varAc==null){
			System.err.println("NULLLLLL>>>" + variableAccess + " " + variableAccess.getPosition());
		}

		return varAc!=null && varAc.equals(variable);
	}

}