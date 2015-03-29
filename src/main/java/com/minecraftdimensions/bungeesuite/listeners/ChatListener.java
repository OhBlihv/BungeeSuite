package com.minecraftdimensions.bungeesuite.listeners;


import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.Utilities;
import com.minecraftdimensions.bungeesuite.managers.ChatManager;
import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Messages;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {
	
	private BungeeSuite instance;
	
	public ChatListener(BungeeSuite instance)
	{
		this.instance = instance;
	}
	
    public static List<String> BlockedCommands = Arrays.asList( "/l", "/lc", "/localchannel", "/lchannel", "/channellocal", "/s", "/sc", "/serverchannel", "/schannel", "/channelserver", "/g", "/globalchat", "/globalchannel", "/gchannel" );

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerLogin( ServerConnectedEvent e ) throws SQLException {
        //        ChatManager.loadPlayersChannels( e.getPlayer(), e.getServer() );
        BSPlayer p = PlayerManager.getPlayer( e.getPlayer() );
        if ( p != null ) {
            p.updateDisplayName();

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerLogin( PostLoginEvent e ) throws SQLException {

    }

    @EventHandler
    public void playerChat( ChatEvent e ) throws SQLException {
        BSPlayer p = PlayerManager.getPlayer( e.getSender().toString() );
        if ( p == null ) {
            if ( e.getSender() instanceof ProxiedPlayer ) {
                ProxiedPlayer player = ( ProxiedPlayer ) e.getSender();
                if ( player != null && player.getPendingConnection() != null ) {
                    PlayerManager.loadPlayer( player );
                } else {
                    return;
                }
            } else {
                return;
            }
        }
        if ( e.isCommand() ) {
            if ( BlockedCommands.contains( e.getMessage().split( " " )[0].toLowerCase() ) ) {
                if ( ChatManager.MuteAll ) {
                    p.sendMessage( Messages.MUTED );
                    e.setCancelled( true );
                }
                if ( p.isMuted() ) {
                    p.sendMessage( Messages.MUTED );
                    e.setCancelled( true );
                    System.out.println( "muted" );
                }
            }
            return;
        }
        if ( ChatManager.MuteAll ) {
            p.sendMessage( Messages.MUTED );
            e.setCancelled( true );
        }
        if ( p.isMuted() ) {
            p.sendMessage( Messages.MUTED );
            e.setCancelled( true );
        }
    }
    
    @EventHandler
    public void onTabComplete(TabCompleteEvent ev)
    {
    	ev.getSuggestions().clear();
        for(ProxiedPlayer player : instance.getProxy().getPlayers())
        {
        	BSPlayer bsplayer = PlayerManager.getPlayer(player);
        	if(player.getName().toLowerCase().startsWith(ev.getCursor().toLowerCase()))
        	{
        		ev.getSuggestions().add(player.getName());
        	}
        	if(bsplayer.hasNickname())
        	{
        		if(bsplayer.getNickname().toLowerCase().startsWith(ev.getCursor().toLowerCase()))
        		{
        			ev.getSuggestions().add(Utilities.stripColours(bsplayer.getNickname()));
        		}
        	}
        }
    }

    //    @EventHandler( priority = EventPriority.HIGHEST )
    //    public void playerLogout( PlayerDisconnectEvent e ) throws SQLException {
    //
    //    }

    /*@EventHandler
    public void playerKicked( ServerKickEvent e ) throws SQLException {
        PlayerManager.kickedPlayers.add( e.getPlayer() );
    }*/

}
