/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.pdb.facts.impl.reference;

import java.util.HashSet;

import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;

class Relation extends Set implements IRelation {

	/* package */Relation(Type type, HashSet<IValue> content) {
		super(TypeFactory.getInstance().relTypeFromTuple(type), content);
	}
	
	public int arity() {
		return getType().getArity();
	}
	
	public IRelation closure() throws FactTypeUseException {
		if (!checkReflexivity()) {
			throw new IllegalOperationException("closure", getType());
		}
		IRelation tmp = (IRelation) this;

		int prevCount = 0;

		while (prevCount != tmp.size()) {
			prevCount = tmp.size();
			tmp = (IRelation) tmp.union(tmp.compose(tmp));
		}

		return tmp;
	}
	
	public IRelation closureStar() throws FactTypeUseException {
		if (!checkReflexivity()) {
			throw new IllegalOperationException("closureStar", getType());
		}
		IRelation closure = closure();
		ISet carrier = carrier();
		Type elementType = carrier.getElementType();
		ISetWriter reflex = Set.createSetWriter(TypeFactory.getInstance().tupleType(elementType, elementType));
		
		for (IValue e: carrier) {
			reflex.insert(new Tuple(new IValue[] {e, e}));
		}
		
		return closure.union(reflex.done());
	}

	public IRelation compose(IRelation other) throws FactTypeUseException {
		Type resultType = getType().compose(other.getType());
		IRelationWriter w = ValueFactory.getInstance().relationWriter(resultType.getFieldTypes());
		int max1 = arity() - 1;
		int max2 = other.arity() - 1;
		int width = max1 + max2;

		for (IValue v1 : content) {
			ITuple t1 = (ITuple) v1;
			for (IValue t2 : other) {
				IValue[] values = new IValue[width];
				ITuple tuple2 = (ITuple) t2;
				if (t1.get(max1).isEqual(tuple2.get(0))) {
					for (int i = 0; i < max1; i++) {
						values[i] = t1.get(i);
					}

					for (int i = max1, j = 1; i < width; i++, j++) {
						values[i] = tuple2.get(j);
					}

					w.insert(new Tuple(values));
				}
			}
		}
		return w.done();
	}

	public ISet carrier() {
		Type newType = checkCarrier();
		ISetWriter w = Set.createSetWriter(newType.getElementType());
		
		for (IValue t : this) {
			w.insertAll((ITuple) t);
		}
		return w.done();
	}

	public ISet domain() {
		Type relType = getType();
		ISetWriter w = Set.createSetWriter(relType.getFieldType(0));
		
		for (IValue elem : this) {
			ITuple tuple = (ITuple) elem;
			w.insert(tuple.get(0));
		}
		return w.done();
	}
	
	public ISet range() {
		Type relType = getType();
		int last = relType.getArity() - 1;
		ISetWriter w = Set.createSetWriter(relType.getFieldType(last));
		
		for (IValue elem : this) {
			ITuple tuple = (ITuple) elem;
			w.insert(tuple.get(last));
		}
		
		return w.done();
	}
	
	private boolean checkReflexivity() throws FactTypeUseException {
		Type type = getType();
		int arity = type.getArity();
		
		if (arity == 2 || arity == 0) {
			Type t1 = type.getFieldType(0);
			Type t2 = type.getFieldType(1);

			return t1.comparable(t2);
		}
		
		return false;
	}


	private Type checkCarrier() {
		Type result = fType.getFieldType(0);
		int width = fType.getArity();
		
		for (int i = 1; i < width; i++) {
			result = result.lub(fType.getFieldType(i));
		}
		
		return TypeFactory.getInstance().setType(result);
	}
	
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		return v.visitRelation(this);
	}
	
	public Type getFieldTypes() {
		return fType.getFieldTypes();
	}
	
	public static IRelationWriter createRelationWriter(Type tupleType) {
		return new RelationWriter(tupleType);
	}
	
	protected static class RelationWriter  extends Set.SetWriter implements IRelationWriter {
		public RelationWriter(Type eltType) {
			super(eltType);
		}
			
		public IRelation done() {
			if(constructedSet == null){
				constructedSet = new Relation(eltType, setContent);
			}
			return  (IRelation) constructedSet;
		}
	}
	
	public ISet select(int... fields) {
		Type eltType = getFieldTypes().select(fields);
		ISetWriter w = ValueFactory.getInstance().setWriter(eltType);
		
		for (IValue v : this) {
			w.insert(((ITuple) v).select(fields));
		}
		
		return w.done();
	}
	
	public ISet select(String... fields) {
		int[] indexes = new int[fields.length];
		int i = 0;
		
		if (getFieldTypes().hasFieldNames()) {
			for (String field : fields) {
				indexes[i++] = getFieldTypes().getFieldIndex(field);
			}
			
			return select(indexes);
		}
		else {
			throw new IllegalOperationException("select with field names", getType());
		}
	}
}