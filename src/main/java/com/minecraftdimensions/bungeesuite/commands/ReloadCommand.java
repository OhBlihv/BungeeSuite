package com.minecraftdimensions.bungeesuite.commands;

import com.minecraftdimensions.bungeesuite.configs.MainConfig;
import com.minecraftdimensions.bungeesuite.objects.Messages;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReloadCommand extends Command
{

    public ReloadCommand() 
    {
        super( "bsreload" );
    }

    @Override
    public void execute( CommandSender sender, String[] args )
    {
        if ( !( sender.hasPermission( "bungeesuite.reload" ) || sender.hasPermission( "bungeesuite.admin" ) ) )
        {
            if ( sender instanceof ProxiedPlayer )
            {
                ProxiedPlayer p = ( ProxiedPlayer ) sender;
                p.chat( "/bsreload" );
            }
        }
        else
        {
            Messages.reloadMessages();
            MainConfig.reloadConfig();
            TextComponent reloadedmsg = new TextComponent("config.yml and messages.yml reloaded.");
            reloadedmsg.setColor(ChatColor.GREEN);
            sender.sendMessage(reloadedmsg);
            //sender.sendMessage(new ComponentBuilder("config.yml and messages.yml reloaded.").color(ChatColor.GREEN).create());
            //sender.sendMessage( "config.yml and messages.yml reloaded!" );
        }

    }

}
