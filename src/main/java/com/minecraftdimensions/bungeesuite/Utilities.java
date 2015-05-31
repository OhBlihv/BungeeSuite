package com.minecraftdimensions.bungeesuite;

import java.util.regex.Pattern;

import com.google.common.net.InetAddresses;

public class Utilities
{

	public static boolean isIPAddress(String ip)
	{
		return InetAddresses.isInetAddress(ip);
	}

	// The same as the regular one, but I've removed the 'magic' formatting.
	public static String colorize(String input)
	{
		// return ChatColor.translateAlternateColorCodes('&', input);
		String fixedString;
		fixedString = Pattern.compile("(?i)&([0-9A-Fa-fL-Ol-oRr])").matcher(input).replaceAll("\u00A7$1");
		return fixedString;
	}

	public static String stripColours(String toFix)
	{
		Pattern chatColorPattern = Pattern.compile("[&](.)");
		String fixedString = chatColorPattern.matcher(toFix).replaceAll("");
		return fixedString;
	}
}
