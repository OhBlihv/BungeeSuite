package com.minecraftdimensions.bungeesuite;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.google.common.net.InetAddresses;

public class Utilities {
	
	public static boolean isIPAddress(String ip){
		return InetAddresses.isInetAddress(ip);
	}

	public static String deCapitalize(String input) {
		String words[] = input.split(" ");
		if (words.length > 0) {
			int count = 0;
			for (String word : words) {
				if (count == 0) {
					words[count] = WordUtils.capitalizeFully(word);
					count++;
				} else {
					if (word.length() > 2) {
						words[count] = word.toLowerCase();
						count++;
					} else if (word.equals("i")) {
						words[count] = WordUtils.capitalize(word);
						count++;
					} else if (word.equalsIgnoreCase("im")
							|| word.equals("i'm")) {
						words[count] = WordUtils.capitalize(word);
						count++;
					} else if (!word.startsWith(":") && !word.equals("XD")) {
						words[count] = word.toLowerCase();
						count++;
					}
				}
			}
		}
		String output = StringUtils.join(words, " ");
		return output;
	}

	//The same as the regular one, but I've removed the 'magic' formatting.
	public static String colorize(String input) {
		//return ChatColor.translateAlternateColorCodes('&', input);
		String fixedString;
		Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-Fa-fL-Ol-oRr])");
		fixedString = chatColorPattern.matcher(input).replaceAll("\u00A7$1");
		return fixedString;
	}
}
