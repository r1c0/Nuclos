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
package org.nuclos.common;
import   java.util.*;

/**
* Used to iterate a collection that can change during iteration.
*
* Items returned by the dynamic collection iterator are items which:
*   - exist in the collection during the iterator creation
*   - AND were not remove from the collection during the iteration
*
*/
public class DynamicCollectionIterator<T> implements Iterator<T> {

    private final Collection<T> collection;
    private final Iterator<T> copyIterator;
    private T nextItem;

    public DynamicCollectionIterator ( Collection<T> collection ) {
        this .collection = collection;
        Collection<T> collectionCopy = new ArrayList<T> ( collection ) ;
        this .copyIterator = collectionCopy.iterator () ;
        this .nextItem = null ;
    }

    @Override
	public boolean hasNext () {
        while ( copyIterator.hasNext ()) {
            T item = copyIterator.next () ;
            if ( collection.contains ( item )) {
                this .nextItem = item;
                return true ;
            }
        }
        return false ;
    }

    @Override
	public T next () {
        if ( nextItem == null )
            if ( !hasNext ())
                return null ;
        try {
            return nextItem;
        }
        finally {
            this .nextItem = null ;
        }
    }

    @Override
	public void remove () {
        throw new UnsupportedOperationException () ;
    }
}
