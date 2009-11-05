/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cipango.ims.oam.util;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;


/**
 * Allows the use of lists with dataview. The only requirement is that either list items must be
 * serializable or model(Object) needs to be overridden to provide the proper model implementation.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * @param <T>
 * 
 */
public abstract class AbstractListDataProvider<T> implements IDataProvider<T>
{
	private static final long serialVersionUID = 1L;

	/** reference to the list used as dataprovider for the dataview */
	private List<T> _list;

	/**
	 * @see IDataProvider#iterator(int, int)
	 */
	public Iterator<? extends T> iterator(final int first, final int count)
	{
		if (_list == null)
			_list = load();
		int toIndex = first + count;
		if (toIndex > _list.size())
		{
			toIndex = _list.size();
		}
		return _list.subList(first, toIndex).listIterator();
	}

	/**
	 * @see IDataProvider#size()
	 */
	public int size()
	{
		if (_list == null)
			_list = load();
		return _list.size();
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	public void detach()
	{
		_list = null;
	}
	
	public abstract List<T> load();

}
