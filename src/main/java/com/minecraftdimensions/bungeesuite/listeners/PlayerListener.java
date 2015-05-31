package com.minecraftdimensions.bungeesuite.listeners;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.Utilities;
import com.minecraftdimensions.bungeesuite.configs.MainConfig;
import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Messages;
import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener
{

	@EventHandler(priority = EventPriority.LOW)
	public void playerLogin(PostLoginEvent e) throws SQLException
	{
		if (!PlayerManager.onlinePlayers.containsKey(e.getPlayer().getName()))
		{
			PlayerManager.loadPlayer(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerLogin(ServerConnectedEvent e)
	{
		BSPlayer p = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
		if (p.firstConnect())
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
					PlayerManager.sendBroadcast(Messages.PLAYER_CONNECT_PROXY.replace("{player}", p.getDisplayingName()));
					break;
			}
			p.connected();

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			try
			{
				out.writeUTF("JoinPlayer");
				out.writeUTF(p.getUUID().toString());
			}
			catch (IOException ex2)
			{
				ex2.printStackTrace();
			}

			Map<String, ServerInfo> servers = BungeeSuite.proxy.getServers();

			// Add the player to every BungeeSuiteChat's onlinePlayers list
			for (String serverName : servers.keySet())
			{
				BungeeSuite.proxy.getScheduler().runAsync(BungeeSuite.instance, new SendPluginMessage("BungeeSuiteChat", servers.get(serverName), b));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerLogout(final PlayerDisconnectEvent e)
	{
		int dcTime = MainConfig.playerDisconnectDelay;
		final BSPlayer p = PlayerManager.getPlayer(e.getPlayer());
		if (dcTime > 0)
		{
			BungeeSuite.proxy.getScheduler().schedule(BungeeSuite.instance, new Runnable()
			{

				@Override
				public void run()
				{
					if (PlayerManager.isPlayerOnline(p.getName()) && ProxyServer.getInstance().getPlayer(e.getPlayer().getName()) == null)
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
								if (!PlayerManager.kickedPlayers.contains(e.getPlayer()))
								{
									PlayerManager.sendBroadcast(Messages.PLAYER_DISCONNECT_PROXY.replace("{player}", p.getDisplayingName()));
								}
								else
								{
									PlayerManager.kickedPlayers.remove(e.getPlayer());
								}
								break;
						}
						
						PlayerManager.unloadPlayer(e.getPlayer());
					}
				}

			}, MainConfig.playerDisconnectDelay, TimeUnit.SECONDS);
		}
		else
		{
			if (PlayerManager.isPlayerOnline(p.getName()) && ProxyServer.getInstance().getPlayer(e.getPlayer().getName()) == null)
			{
				if (!PlayerManager.kickedPlayers.contains(e.getPlayer()))
				{
					PlayerManager.sendBroadcast(Utilities.colorize("&6&l|| &b«« &cBye for now &6{player}!").replace("{player}", p.getDisplayingName()));
				}
				else
				{
					PlayerManager.kickedPlayers.remove(e.getPlayer());
				}
				PlayerManager.unloadPlayer(e.getPlayer());
			}
		}
	}
}
