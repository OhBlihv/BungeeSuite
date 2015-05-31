package com.minecraftdimensions.bungeesuite.objects;

public class Channel
{
	private String name;
	private String format;

	public Channel(String name, String format)
	{
		this.name = name;
		this.format = format;
	}

	public Channel(String serialised)
	{
		String data[] = serialised.split("~");
		name = data[0];
		format = data[1];
	}

	public String getName()
	{
		return name;
	}

	public String format()
	{
		return format;
	}

	public boolean isDefault()
	{
		return true;
	}

	public String serialise()
	{
		return name + "~" + format;
	}
}
