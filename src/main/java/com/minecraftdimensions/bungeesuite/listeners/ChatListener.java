package com.minecraftdimensions.bungeesuite.listeners;

import com.minecraftdimensions.bungeesuite.managers.ChatManager;
import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Messages;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;

public class ChatListener implements Listener
{

	public ChatListener()
	{

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerLogin(ServerConnectedEvent e)
	{
		BSPlayer p = PlayerManager.getPlayer(e.getPlayer());
		if (p != null)
		{
			p.updateDisplayName();
		}
	}

	@EventHandler
	public void playerChat(ChatEvent e) throws SQLException
	{
		BSPlayer p = PlayerManager.getPlayer(e.getSender().toString());
		if (p == null)
		{
			if (e.getSender() instanceof ProxiedPlayer)
			{
				ProxiedPlayer player = (ProxiedPlayer) e.getSender();
				if (player != null && player.getPendingConnection() != null)
				{
					PlayerManager.loadPlayer(player);
					return;
				}
				
				return;
			}
			
			return;
		}
		if (ChatManager.MuteAll)
		{
			p.sendMessage(Messages.MUTED);
			e.setCancelled(true);
		}
		if (p.isMuted())
		{
			p.sendMessage(Messages.MUTED);
			e.setCancelled(true);
		}
	}

}
