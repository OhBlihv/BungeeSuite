package com.minecraftdimensions.bungeesuite.commands;

import com.minecraftdimensions.bungeesuite.BungeeSuite;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class AliasCommand extends Command
{
	
	public AliasCommand() 
    {
        super("alias");
    }

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if ((sender.hasPermission("bungeesuite.uuid"))) 
        {
			TextComponent msg;
			if(args.length <= 1)
			{
				msg = new TextComponent("Finding your previous aliases...");
				msg.setColor(ChatColor.GOLD);
				sender.sendMessage(msg);
				BungeeSuite.instance.getAliases(sender.getName(), sender);
			}
			else
			{
				msg = new TextComponent("Finding previous aliases of " + args[1] + "...");
				msg.setColor(ChatColor.GOLD);
				sender.sendMessage(msg);
				BungeeSuite.instance.getAliases(args[1], sender);
			}
			
        }
	}
}
