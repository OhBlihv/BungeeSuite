package com.minecraftdimensions.bungeesuite.listeners;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.Utilities;
import com.minecraftdimensions.bungeesuite.configs.MainConfig;
import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Messages;
import com.minecraftdimensions.bungeesuite.managers.ChatManager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener
{

	@EventHandler(priority = EventPriority.LOW)
	public void playerLogin(PostLoginEvent event) throws SQLException
	{
		if (!PlayerManager.onlinePlayers.containsKey(event.getPlayer().getName()))
		{
			PlayerManager.loadPlayer(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerLogin(ServerConnectedEvent event)
	{
		BSPlayer player = PlayerManager.getPlayer(event.getPlayer().getUniqueId());
		if (player.firstConnect())
		{
			String name = player.getName();
			switch (name)
			{
				case "OhBlihv":
				case "StabbyInc":
				case "CAMM_":
				case "Jedi_Vader20":
				case "LikingSquares":
				case "Mallorean":
				case "Blivvykins":
				case "Obliviator":
					break;
				default:
					PlayerManager.sendBroadcast(Messages.PLAYER_CONNECT_PROXY.replace("{player}", player.getDisplayingName()));
					break;
			}
			player.connected();

			Map<String, ServerInfo> servers = BungeeSuite.proxy.getServers();

			// Add the player to every BungeeSuiteChat's onlinePlayers list
			for (String serverName : servers.keySet())
			{
				ChatManager.sendPlayer(player, servers.get(serverName), false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerLogout(final PlayerDisconnectEvent event)
	{
		final BSPlayer p = PlayerManager.getPlayer(event.getPlayer());
		if (MainConfig.playerDisconnectDelay > 0)
		{
			BungeeSuite.proxy.getScheduler().schedule(BungeeSuite.instance, new Runnable()
			{

				@Override
				public void run()
				{
					if (PlayerManager.isPlayerOnline(p.getName()) && ProxyServer.getInstance().getPlayer(event.getPlayer().getName()) == null)
					{
						String name = p.getName();
						switch (name)
						{
							case "OhBlihv":
							case "StabbyInc":
							case "CAMM_":
							case "Jedi_Vader20":
							case "LikingSquares":
							case "Mallorean":
							case "Blivvykins":
							case "Obliviator":
								break;
							default:
								if (!PlayerManager.kickedPlayers.contains(event.getPlayer()))
								{
									PlayerManager.sendBroadcast(Messages.PLAYER_DISCONNECT_PROXY.replace("{player}", p.getDisplayingName()));
								}
								else
								{
									PlayerManager.kickedPlayers.remove(event.getPlayer());
								}
								break;
						}
						
						PlayerManager.unloadPlayer(event.getPlayer());
						
						Map<String, ServerInfo> servers = BungeeSuite.proxy.getServers();

						// Remove the player from every BungeeSuiteChat's onlinePlayers list
						for (String serverName : servers.keySet())
						{
							ChatManager.unloadPlayer(name, servers.get(serverName));
						}
					}
				}

			}, MainConfig.playerDisconnectDelay, TimeUnit.SECONDS);
		}
		else
		{
			if (PlayerManager.isPlayerOnline(p.getName()) && ProxyServer.getInstance().getPlayer(event.getPlayer().getName()) == null)
			{
				if (!PlayerManager.kickedPlayers.contains(event.getPlayer()))
				{
					PlayerManager.sendBroadcast(Utilities.colorize("&6&l|| &b«« &cBye for now &6{player}!").replace("{player}", p.getDisplayingName()));
				}
				else
				{
					PlayerManager.kickedPlayers.remove(event.getPlayer());
				}
				PlayerManager.unloadPlayer(event.getPlayer());
				
				Map<String, ServerInfo> servers = BungeeSuite.proxy.getServers();

				// Remove the player from every BungeeSuiteChat's onlinePlayers list
				for (String serverName : servers.keySet())
				{
					ChatManager.unloadPlayer(p.getName(), servers.get(serverName));
				}
			}
		}
	}
}
