// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.ims.hss.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class XML
{
	private static XML __default = new XML(false);
	private static XML __pretty = new XML(true);
	
	private Map<Class<?>, Convertor> _convertors = new HashMap<Class<?>, Convertor>();
	
	private boolean _prettyPrint;
	
	public static XML getDefault()
    {
        return __default;
    }
	
	public static XML getPretty()
    {
        return __pretty;
    }
	
	public XML(boolean prettyPrint)
	{
		_prettyPrint = prettyPrint;
	}
	
	public String toXml(String name, Object value)
	{
		OutputImpl output = new OutputImpl();
		output.add(name, value);
		return output.toString();
	}
	
	public Output newOutput()
	{
		return new OutputImpl();
	}

    /**
     * Lookup a convertor for a class.
     * <p>
     * If no match is found for the class, then the interfaces for the class are tried. If still no
     * match is found, then the super class and it's interfaces are tried recursively.
     * @param forClass The class
     * @return a {@link Convertor} or null if none were found.
     */
    protected Convertor getConvertor(Class<?> forClass)
	{
		Class<?> c = forClass;
		Convertor convertor = _convertors.get(c);
		if (convertor == null && this != __default)
			convertor = __default.getConvertor(forClass);

		while (convertor == null && c != null && c != Object.class)
		{
			Class<?>[] ifs = c.getInterfaces();
			int i = 0;
			while (convertor == null && ifs != null && i < ifs.length)
				convertor = _convertors.get(ifs[i++]);
			if (convertor == null)
			{
				c = c.getSuperclass();
				convertor = _convertors.get(c);
			}
		}
		return convertor;
    }
	

    public interface Output
    {
    	public void open(String name);
    	public void close(String name);
        public void add(String name, Object value);
    }
    
    public interface Convertible
    {
        public void print(Output out);
    }
    
    public interface Convertor
    {
        public void print(Object obj, Output out);
    }
    
    public class OutputImpl implements Output
    {
    	private StringBuilder _sb = new StringBuilder();
    	private int _indexTab;
    	
    	public void open(String name)
    	{
    		if (_prettyPrint)
    		{
    			for (int i = 0; i < _indexTab; i++)
    				_sb.append('\t');
    			_indexTab++;
    		}
    		_sb.append('<').append(name).append(">");
    		if (_prettyPrint)
    		{
    			_sb.append('\n');
    		}
    	}
    	    	
    	public void close(String name)
    	{
    		if (_prettyPrint)
    		{
    			_indexTab--;
    			for (int i = 0; i < _indexTab; i++)
    				_sb.append('\t');
    		}
    		_sb.append("</").append(name).append(">");
    		if (_prettyPrint)
    			_sb.append('\n');
    	}
    	
		public void add(String name, Object value)
		{
			if (value == null)
			{
				if (_prettyPrint)
	    		{
	    			for (int i = 0; i < _indexTab; i++)
	    				_sb.append('\t');
	    		}
				_sb.append('<').append(name).append("/>");
				if (_prettyPrint)
	    		{
	    			_sb.append('\n');
	    		}
			} 
			else 
			{
				if (value instanceof Collection<?>)
				{
					Iterator<?> it = ((Collection<?>) value).iterator();
					while (it.hasNext())
					{
						Object val = (Object) it.next();
						add(name, val);
					}
				}
				else
				{
					if (_prettyPrint)
		    		{
		    			for (int i = 0; i < _indexTab; i++)
		    				_sb.append('\t');
		    			_indexTab++;
		    		}
					_sb.append('<').append(name).append('>');
					if (value instanceof Boolean)
					{
						_sb.append(((Boolean) value) ? 1 : 0);
					}
					else if (value instanceof String || value.getClass().isPrimitive())
					{
						_sb.append(value);
					}
					else if (value instanceof Convertible)
					{
						if (isPrettyPrint())
							_sb.append('\n');
						((Convertible) value).print(this);
						if (_prettyPrint)
			    		{
							for (int i = 1; i < _indexTab; i++)
			    				_sb.append('\t');
			    		}
					}
			        else
			        {
			            Convertor convertor=getConvertor(value.getClass());
			            if (convertor!=null)
			            {
			            	if (isPrettyPrint())
								_sb.append('\n');
			            	convertor.print(value, this);
							if (_prettyPrint)
				    		{
								for (int i = 1; i < _indexTab; i++)
				    				_sb.append('\t');
				    		}
			            }
			            else
			            	_sb.append(value);
			        }
					_sb.append("</").append(name).append('>');
		    		if (_prettyPrint)
		    		{
		    			_indexTab--;
		    			_sb.append('\n');
		    		}
				}
			}
			
		}
		
		public String toString()
		{
			return _sb.toString();
		}

    	
    }

	public boolean isPrettyPrint()
	{
		return _prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint)
	{
		_prettyPrint = prettyPrint;
	}
}
