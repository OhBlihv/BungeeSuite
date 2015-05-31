package com.minecraftdimensions.bungeesuite.objects;

public class ServerData
{
	String serverName;
	String shortName;
	final boolean forceChannel = true;
	final String forcedChannel = "Global";
	final boolean connectionMessages = true;

	public ServerData(String name, String shortName)
	{
		this.serverName = name;
		this.shortName = shortName;
	}

	public String getServerName()
	{
		return serverName;
	}

	public String getServerShortName()
	{
		return shortName;
	}

	public String getForcedChannel()
	{
		return "Global";
	}
}
