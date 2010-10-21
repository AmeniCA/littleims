package org.cipango.ims.hss.util;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;

public class CustomNamingStrategy extends ImprovedNamingStrategy implements NamingStrategy
{
	public String propertyToColumnName(String propertyName)
	{
		if (propertyName.startsWith("_"))
			return super.propertyToColumnName(propertyName.substring(1));
		else
			return super.propertyToColumnName(propertyName);
	}
	
	public String columnName(String columnName)
	{
		if (columnName.startsWith("_"))
			return super.columnName(columnName.substring(1));
		else
			return super.columnName(columnName);
	}
}
