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

package org.eclipse.imp.pdb.facts.impl;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;

/*package*/ class IntegerValue extends Value implements IInteger {
    private final int fValue;

    /*package*/ IntegerValue(int i) {
        super(TypeFactory.getInstance().integerType());
        fValue= i;
    }
    
    private IntegerValue(IntegerValue other, String label, IValue anno) {
    	super(other, label, anno);
    	fValue = other.fValue;
    }

	public int getValue() {
        return fValue;
    }

    @Override
    public String toString() {
        return Integer.toString(fValue);
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof IntegerValue) {
    		return ((IntegerValue) o).fValue == fValue;
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
    	return fValue;
    }
    
    public <T> T accept(IValueVisitor<T> v) throws VisitorException {
    	return v.visitInteger(this);
    }
    
    @Override
    protected IValue clone(String label, IValue anno)  {
    	return new IntegerValue(this, label, anno);
    }
}
