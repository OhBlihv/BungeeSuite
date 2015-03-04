package com.minecraftdimensions.bungeesuite.commands;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.managers.*;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * User: Bloodsplat
 * Date: 13/10/13
 */
public class BSVersionCommand extends Command
{

    public BSVersionCommand() 
    {
        super( "bsversion" );
    }

    @Override
    public void execute( CommandSender sender, String[] args )
    {
        if ( !( sender.hasPermission( "bungeesuite.version" ) || sender.hasPermission( "bungeesuite.admin" ) ) ) 
        {
            ProxiedPlayer p = ( ProxiedPlayer ) sender;
            p.chat( "/bsversion" );
        } 
        else 
        {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream( b );
            try {
                out.writeUTF( "GetVersion" );

            } catch ( IOException e ) {
                e.printStackTrace();
            }

            //sender.sendMessage(new ComponentBuilder("BungeeSuite Version:  ").color(ChatColor.GOLD).bold(true).append(BungeeSuite.instance.getDescription().getVersion()).create());
            //Creation of the TextComponents
            TextComponent message = new TextComponent("AusCraft BungeeSuite Version: ");
            message.setColor(ChatColor.GOLD);
            message.setBold(true);
            message.addExtra(BungeeSuite.instance.getDescription().getVersion());
            
            sender.sendMessage(message);
            //sender.sendMessage( ChatColor.RED + "BungeeSuite version - " + ChatColor.GOLD + BungeeSuite.instance.getDescription().getVersion() );
            if ( sender instanceof ProxiedPlayer ) {
                ProxiedPlayer p = ( ProxiedPlayer ) sender;
                try {
                    out.writeUTF( p.getName() );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                ServerInfo s = p.getServer().getInfo();
                ChatManager.sendPluginMessageTaskChat( s, b );
            } else {
                if ( args.length == 0 ) {
                    return;
                } else {
                    ServerInfo s = ProxyServer.getInstance().getServerInfo( args[0] );
                    if ( s == null ) {
                    	//sender.sendMessage(new ComponentBuilder("Server does not exist!").color(ChatColor.RED).create());
                    	TextComponent dnexist = new TextComponent("Server does not exist!");
                    	dnexist.setColor(ChatColor.RED);
                    	sender.sendMessage(dnexist);
                        //sender.sendMessage( ChatColor.RED + "Server does not exist" );
                        return;
                    }
                    if ( s.getPlayers().size() == 0 ) {
                    	TextComponent serveroffline = new TextComponent("That server is either offline, or there are no players on it.");
                    	serveroffline.setColor(ChatColor.RED);
                    	sender.sendMessage(serveroffline);
                    	//sender.sendMessage(new ComponentBuilder("That server is either offline, or there are no players on it.").color(ChatColor.RED).create());
                        //sender.sendMessage( ChatColor.RED + "That server is either offline or there are no players on it" );
                        return;
                    }
                    ChatManager.sendPluginMessageTaskChat( s, b );
                }
            }
        }

    }
}
