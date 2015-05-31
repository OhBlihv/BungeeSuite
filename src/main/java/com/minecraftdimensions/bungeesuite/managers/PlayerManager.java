package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.Utilities;
import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Messages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.ChatColor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerManager
{

	public static HashMap<String, BSPlayer> onlinePlayers = new HashMap<>();
	public static HashMap<UUID, BSPlayer> onlinePlayersUUID = new HashMap<>();
	static ProxyServer proxy = ProxyServer.getInstance();
	static BungeeSuite plugin = BungeeSuite.instance;
	public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<ProxiedPlayer>();

	public static boolean playerExists(UUID uuid, String player)
	{
		try
		{
			LoggingManager.log("UUID: " + uuid + " for " + player);
			if (!SQLManager.existanceQuery("SELECT uuid FROM BungeePlayers WHERE uuid = '" + uuid + "'"))
			{
				return SQLManager.existanceQuery("SELECT playername FROM BungeePlayers WHERE playername = '" + player + "'");
			}
			return true;
		}
		catch (NullPointerException e)
		{
			LoggingManager.log("UUID" + uuid + " is not attached to any previous player");
		}
		return false;
	}

	public static boolean playerExists(String player)
	{
		if (getSimilarPlayer(player) != null)
		{
			return true;
		}
		return SQLManager.existanceQuery("SELECT playername FROM BungeePlayers WHERE playername = '" + player + "'");
	}

	public static void loadPlayer(ProxiedPlayer player) throws SQLException
	{
		String playername = player.getName();
		String nickname = null;
		boolean muted = false;
		UUID uuid = null;

		if (playerExists(player.getUniqueId(), playername))
		{
			ResultSet res = null;
			boolean uuidRecord = false;

			// UUID
			if (SQLManager.existanceQuery("SELECT playername FROM BungeePlayers WHERE UUID = '" + player.getUniqueId().toString() + "'"))
			{
				res = SQLManager.sqlQuery("SELECT playername,nickname,muted,UUID FROM BungeePlayers WHERE UUID = '" + player.getUniqueId().toString() + "'");
				uuidRecord = true;
			}
			// Playername
			else if (SQLManager.existanceQuery("SELECT playername FROM BungeePlayers WHERE playername = '" + player.getName() + "'"))
			{
				res = SQLManager.sqlQuery("SELECT playername,nickname,muted,UUID FROM BungeePlayers WHERE playername = '" + player.getName() + "'");
			}
			else
			{
				return;
			}

			while (res.next())
			{
				nickname = res.getString("nickname");
				if (nickname != null)
				{
					nickname = Utilities.colorize(nickname);
				}
				muted = res.getBoolean("muted");
				String uuidNull = res.getString("UUID");
				if (uuidNull != null)
				{
					uuid = UUID.fromString(uuidNull);
				}
				else
				{
					uuid = player.getUniqueId();
				}
				if (!uuidRecord)
				{
					checkUUID(uuid, playername);
				}
				playername = res.getString("playername");
				if (!playername.equals(player.getName()))
				{
					if (uuidNull != null)
					{
						changePlayerName(uuidNull, player.getName());
					}
					playername = player.getName();
				}
			}
			res.close();
			BSPlayer bsplayer = new BSPlayer(playername, nickname, "Global", muted, uuid);
			addPlayer(bsplayer);
			IgnoresManager.LoadPlayersIgnores(bsplayer);
		}
		else
		{
			createNewPlayer(player);
		}
	}

	private static void createNewPlayer(final ProxiedPlayer player) throws SQLException
	{
		String ip = player.getAddress().getAddress().toString();
		UUID uuid = player.getUniqueId();
		SQLManager.standardQuery("INSERT INTO BungeePlayers (playername,lastonline,ipaddress,UUID) VALUES ('" + player.getName() + "', NOW(), '" + ip.substring(1, ip.length()) + "','" + uuid + "')");
		final BSPlayer bsplayer = new BSPlayer(player.getName(), null, ChatConfig.defaultChannel, false, uuid);
		sendBroadcast(Messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getName()));

		// Taken from addplayer
		onlinePlayersUUID.put(bsplayer.getUUID(), bsplayer);
		onlinePlayers.put(bsplayer.getName(), bsplayer);
	}

	private static void addPlayer(BSPlayer player)
	{
		onlinePlayersUUID.put(player.getUUID(), player);
		onlinePlayers.put(player.getName(), player);
		if (!player.firstConnect())
		{
			String login = Utilities.colorize("&6&l|| &b«« &aWelcome back &6{player}!");
			LoggingManager.log(login.replace("{player}", player.getName()));
		}
	}

	public static void unloadPlayer(ProxiedPlayer player)
	{
		if (onlinePlayers.containsKey(player.getName()))
		{
			onlinePlayers.remove(player.getName());
			onlinePlayersUUID.remove(player.getUniqueId());
			String logout = Utilities.colorize("&6&l|| &b«« &cBye for now &6{player}!");
			LoggingManager.log(logout.replace("{player}", player.getName()));
		}
	}

	public static BSPlayer getPlayer(String player)
	{
		return onlinePlayers.get(player);
	}

	public static BSPlayer getPlayer(UUID uuid)
	{
		return onlinePlayersUUID.get(uuid);
	}

	public static BSPlayer getSimilarPlayer(String playerName)
	{
		BSPlayer player = null;
		{
			player = onlinePlayers.get(playerName);
		}
		if(player != null)
		{
			for (String playerLoop : onlinePlayers.keySet())
			{
				if (ChatColor.stripColor(Utilities.stripColours(playerLoop.toLowerCase())).equals(ChatColor.stripColor(Utilities.stripColours(playerName.toLowerCase()))))
				{
					return onlinePlayers.get(playerLoop);
				}
			}
		}
		else
		{
			return player;
		}
		return null;
	}

	// UUID
	public static void setUUID(UUID uuid, String playername)
	{
		try
		{
			SQLManager.standardQuery("UPDATE BungeePlayers SET UUID = '" + uuid + "' WHERE playername = '" + playername + "'");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void setUUID(String playerName)
	{
		BSPlayer player = getPlayer(playerName);
		setUUID(player.getUUID(), playerName);
	}

	public static void checkUUID(UUID uuid, String playername)
	{
		try
		{
			if (!SQLManager.existanceQuery("SELECT uuid FROM BungeePlayers WHERE uuid = '" + uuid + "'"))
			{
				SQLManager.standardQuery("UPDATE BungeePlayers SET UUID = '" + uuid + "' WHERE playername = '" + playername + "'");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	// Used when a player joins the server with a different name to their
	// previous one (identified by UUID)
	public static void changePlayerName(BSPlayer player, String newName)
	{
		player.setPlayerName(newName);
	}

	// Changes SQL
	public static void changePlayerName(String UUID, String newName)
	{
		try
		{
			SQLManager.standardQuery("UPDATE BungeePlayers SET playername = '" + newName + "' WHERE UUID = '" + UUID + "'");
			LoggingManager.log("Successfully updated " + newName + "'s IGN");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void sendPrivateMessageToPlayer(BSPlayer from, String receiver, String message)
	{
		BSPlayer rec = getSimilarPlayer(receiver);
		if (from.isMuted() && ChatConfig.mutePrivateMessages)
		{
			from.sendMessage(Messages.MUTED);
			return;
		}
		if (rec == null)
		{
			from.sendMessage(Messages.PLAYER_NOT_ONLINE);
			return;
		}

		if (rec.isIgnoring(from.getName()))
		{
			from.sendMessage(Messages.PLAYER_IGNORING.replace("{player}", rec.getName()));
			return;
		}
		from.sendMessage(Messages.PRIVATE_MESSAGE_OTHER_PLAYER.replace("{player}", rec.getName()).replace("{message}", message));
		rec.sendMessage(Messages.PRIVATE_MESSAGE_RECEIVE.replace("{player}", from.getName()).replace("{message}", message));
		rec.setReplyPlayer(from.getName());
		sendPrivateMessageToSpies(from, rec, message);
	}

	public static void sendMessageToPlayer(String player, String message)
	{
		if (player.equals("CONSOLE"))
		{
			ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(message));
		}
		else
		{
			for (String line : message.split("\n"))
			{
				getPlayer(player).sendMessage(line);
			}
		}
	}

	public static String getPlayersIP(String player) throws SQLException
	{
		BSPlayer p = getSimilarPlayer(player);
		String ip = null;
		if (p == null)
		{
			ResultSet res = SQLManager.sqlQuery("SELECT ipaddress FROM BungeePlayers WHERE playername = '" + player + "'");
			while (res.next())
			{
				ip = res.getString("ipaddress");
			}
			res.close();
		}
		else
		{
			ip = p.getProxiedPlayer().getAddress().getAddress().toString();
			ip = ip.substring(1, ip.length());
		}
		return ip;
	}

	public static void sendBroadcast(String message)
	{
		for (ProxiedPlayer p : proxy.getPlayers())
		{
			for (String line : message.split("\n"))
			{
				p.sendMessage(line);
			}
		}
		LoggingManager.log(message);
	}

	public static boolean isPlayerOnline(String player)
	{
		return onlinePlayers.containsKey(player);
	}

	public static boolean isSimilarPlayerOnline(String player)
	{
		return getSimilarPlayer(player) != null;
	}

	public static BSPlayer getPlayer(CommandSender sender)
	{
		return onlinePlayers.get(sender.getName());
	}

	public static void setPlayerAFK(String player)
	{
		BSPlayer p = getPlayer(player);
		if (!p.isAFK())
		{
			p.setAFK(true);
			sendBroadcast(Messages.PLAYER_AFK.replace("{player}", p.getDisplayingName()));
		}
		else
		{
			p.setAFK(false);
			sendBroadcast(Messages.PLAYER_NOT_AFK.replace("{player}", p.getDisplayingName()));
		}

	}

	public static ArrayList<BSPlayer> getChatSpies()
	{
		ArrayList<BSPlayer> spies = new ArrayList<>();
		for (BSPlayer p : onlinePlayers.values())
		{
			if (p.isChatSpying())
			{
				spies.add(p);
			}
		}
		return spies;
	}

	public static void sendPrivateMessageToSpies(BSPlayer sender, BSPlayer receiver, String message)
	{
		for (BSPlayer p : getChatSpies())
		{
			if (!(p.equals(sender) || p.equals(receiver)))
			{
				p.sendMessage(Messages.PRIVATE_MESSAGE_SPY.replace("{sender}", sender.getName()).replace("{player}", receiver.getName()).replace("{message}", message));
			}
		}
	}

	public static void sendMessageToSpies(Server server, String message)
	{
		for (BSPlayer p : getChatSpies())
		{
			if (!p.getServer().getInfo().getName().equals(server.getInfo().getName()))
			{
				p.sendMessage(message);
			}
		}
	}

	public static boolean nickNameExists(String nick)
	{
		return SQLManager.existanceQuery("SELECT nickname FROM BungeePlayers WHERE nickname ='" + nick + "'");
	}

	public static void setPlayersNickname(String p, String nick) throws SQLException
	{
		if (isPlayerOnline(p))
		{
			getPlayer(p).setNickname(nick);
			getPlayer(p).updateDisplayName();
			getPlayer(p).updatePlayer();
		}
		if (nick == null)
		{
			SQLManager.standardQuery("UPDATE BungeePlayers SET nickname = NULL WHERE playername ='" + p + "'");
		}
		else
		{
			SQLManager.standardQuery("UPDATE BungeePlayers SET nickname ='" + nick + "' WHERE playername ='" + p + "'");
		}
	}

	public static boolean isPlayerMuted(String target)
	{
		if (getSimilarPlayer(target) != null)
		{
			return getPlayer(target).isMuted();
		}
		return SQLManager.existanceQuery("SELECT muted FROM BungeePlayers WHERE playername ='" + target + "' AND muted = 1");

	}

	public static void mutePlayer(String target) throws SQLException
	{
		BSPlayer p = getSimilarPlayer(target);
		boolean isMuted = isPlayerMuted(target);
		if (p != null)
		{
			if (isMuted)
			{
				p.setMute(false);
				p.sendMessage(Messages.UNMUTED);
			}
			else
			{
				p.setMute(true);
				p.sendMessage(Messages.MUTED);
			}
		}
		SQLManager.standardQuery("UPDATE BungeePlayers SET muted = " + !isMuted + " WHERE playername ='" + target + "'");

	}

	public static void tempMutePlayer(final BSPlayer t, int minutes) throws SQLException
	{
		mutePlayer(t.getName());
		BungeeSuite.proxy.getScheduler().schedule(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if (t.isMuted())
				{
					try
					{
						mutePlayer(t.getName());
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}

			}
		}, minutes, TimeUnit.MINUTES);
	}

	public static boolean playerUsingNickname(String string)
	{
		//return SQLManager.existanceQuery("SELECT playername FROM BungeePlayers WHERE nickname LIKE '%" + string + "%'");
		return SQLManager.existanceQuery("SELECT nickname FROM BungeePlayers WHERE nickname ='" + string + "'");
	}

	public static void removeNickname(String target) throws SQLException
	{
		setPlayersNickname(target, null);
	}

	public static Collection<BSPlayer> getPlayers()
	{
		return onlinePlayers.values();
	}

	public static BSPlayer getSimilarNickPlayer(String nick)
	{
		for (BSPlayer p : onlinePlayers.values())
		{
			if (ChatColor.stripColor(p.getNickname()).toLowerCase().contains(nick.toLowerCase()))
			{
				return p;
			}
		}
		return getSimilarPlayer(nick);
	}

}
