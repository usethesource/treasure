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

package org.eclipse.imp.pdb.facts.type;

import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException;


/*package*/ final class RelationType extends SetType {
    /*package*/ final Type fTupleType;

    /**
     * Create a new relation type from a tuple type.
     * @param tupleType
     */
    /*package*/ RelationType(Type tupleType) {
        super(tupleType);
        fTupleType = tupleType;
    }
    
    @Override
    public int getArity() {
    	return fTupleType.getArity();
    }
    
    @Override
    public Type getFieldType(int i) {
    	return fTupleType.getFieldType(i);
    }
    
    @Override
    public Type getFieldType(String label) {
    	return fTupleType.getFieldType(label);
    }
    
    @Override
    public int getFieldIndex(String fieldName) {
    	return fTupleType.getFieldIndex(fieldName);
    }
    
    @Override
    public boolean hasField(String fieldName) {
    	return fTupleType.hasField(fieldName);
    }
    
    @Override
    public Type getFieldTypes() {
    	return fTupleType;
    }
    
    @Override
    public String getFieldName(int i) {
		return fTupleType.getFieldName(i);
    }
    
    @Override
    public boolean isRelationType() {
    	return true;
    }
    
    @Override
    public boolean isSubtypeOf(Type o) {
        if (o.isRelationType()) {
        	return fTupleType.isSubtypeOf(o.getFieldTypes());
        }
        else {
        	return super.isSubtypeOf(o);
        }
    }

    @Override
    public Type lub(Type o) {
    	if (o.isRelationType()) {
    		return TypeFactory.getInstance().setType(fTupleType.lub(o.getFieldTypes()));
    	}
    	else {
    		return super.lub(o);
    	}
    }

    @Override
    public int hashCode() {
        return 58453 + 14323 * fTupleType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RelationType))
            return false;
        RelationType other= (RelationType) obj;
        // N.B.: The element type must have been created and canonicalized before any
        // attempt to manipulate the outer type (i.e. SetType), so we can use object
        // identity here for the fEltType.
        return fTupleType == other.fTupleType;
    }

    @Override
    public String toString() {
    	StringBuffer b = new StringBuffer();

    	b.append("rel[");
    	int idx = 0;
    	for (Type t : fTupleType) {
    		if (idx++ > 0) {
    			b.append(", ");
    		}
    		b.append(t.toString());
    	}
    	b.append("]");
    	return b.toString();
    }

	private boolean isReflexive() throws FactTypeUseException {
		if (getArity() == 2) {
			Type t1 = getFieldType(0);
			Type t2 = getFieldType(1);

			return t1.isSubtypeOf(t2) || t1.isSubtypeOf(t2);
		}
		
		return false;
	}
	
	@Override
	public boolean hasFieldNames() {
		return fTupleType.hasFieldNames();
	}
	
	@Override
	public Type compose(Type other) throws FactTypeUseException {
		if (this == other && isReflexive()) {
			return this;
		}
		else {
			if (getArity() == 1 || other.getArity() == 1) {
				throw new IllegalOperationException("compose", this, other);
			}
		}
		Type t1 = fTupleType;
		Type t2 = other.getFieldTypes();

		Type last = t1.getFieldType(t1.getArity() - 1);
		Type first = t2.getFieldType(0);
		
		if (!(last.isSubtypeOf(first) || first.isSubtypeOf(last))) {
			throw new IllegalOperationException("compose", this, other);
		}
		
		return TypeFactory.getInstance().relTypeFromTuple(t1.compose(t2));
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitRelationType(this);
	}

	@Override
	public IValue make(IValueFactory f) {
		return f.relation(fTupleType);
	}
	
	@Override
	public IValue make(IValueFactory f, IValue...elems) {
		return f.relation(elems);
	}
	
	@Override
	public IRelationWriter writer(IValueFactory f) {
		return f.relationWriter(fTupleType);
	}
}
