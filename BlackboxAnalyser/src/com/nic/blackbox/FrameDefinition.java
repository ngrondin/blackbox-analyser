package com.nic.blackbox;

import java.util.ArrayList;

public class FrameDefinition
{
	protected ArrayList<String> fieldNames;
	protected ArrayList<Integer> fieldEncodings;
	
	public FrameDefinition()
	{
		fieldNames = new ArrayList<String>();
		fieldEncodings = new ArrayList<Integer>();
	}
	
	public void setNames(String[] names)
	{
		fieldNames.clear();
		for(int i = 0; i < names.length; i++)
			fieldNames.add(names[i]);
	}
	
	public void setEncodings(int[] encodings)
	{
		if(encodings.length == fieldNames.size())
		{
			fieldEncodings.clear();
			for(int i = 0; i < encodings.length; i++)
				fieldEncodings.add(encodings[i]);
		}
	}
	
	public String getName(int index)
	{
		return fieldNames.get(index);
	}
	
	public int getEncoding(int index)
	{
		return fieldEncodings.get(index);
	}
	
	public int getFieldCount()
	{
		return fieldNames.size();
	}
}
