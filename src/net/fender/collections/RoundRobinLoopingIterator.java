/*
 * Copyright 2008 - 2009 Eric Fenderbosch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fender.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This doesn't really conform to the general Iterator interface, but simply
 * provides a way for multiple threads to "iterate" through a collection's
 * elements in a round robin fashion in a reasonably cheap and safe way. NB:
 * hasNext() always returns true.
 * 
 * @author Eric Fenderbosch
 * @param <T>
 */
public class RoundRobinLoopingIterator<T> implements Iterator<T> {

	// keep a reference to size around so we don't have to call objects.size()
	// every call to getNext()
	private int size;
	// using an AtomicInteger should be faster/cheaper than synchronzied blocks
	// around someInt++
	private final AtomicInteger index = new AtomicInteger(0);
	private Object[] objects;

	public RoundRobinLoopingIterator(Collection<T> collection) {
		if (collection == null || collection.size() == 0) {
			throw new IllegalArgumentException("collection must be non-null and have size > 0");
		} else {
			size = collection.size();
			objects = new Object[size];
			Iterator<T> iterator = collection.iterator();
			int index = 0;
			while (iterator.hasNext()) {
				objects[index++] = iterator.next();
			}
		}
	}

	/**
	 * Always returns true
	 */
	public boolean hasNext() {
		return true;
	}

	/**
	 * Technically, this isn't perfect because the % operator makes the index
	 * lookup non-atomic, but that's acceptable for a round robin
	 * implementation. We just want to evenly spread out the use of the
	 * elements, and make a reasonable effort to not return the same element in
	 * a very short time frame, not guarantee that each element is only being
	 * used by one thread at a time.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T next() {
		// AtomicInteger gracefully handles incrementing past Integer.MAX_VALUE
		return (T) objects[Math.abs(index.getAndIncrement() % size)];
	}

	public int size() {
		return size;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
