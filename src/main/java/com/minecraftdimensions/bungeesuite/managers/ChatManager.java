package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.Utilities;
import com.minecraftdimensions.bungeesuite.configlibrary.Config;
import com.minecraftdimensions.bungeesuite.configs.Channels;
import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
import com.minecraftdimensions.bungeesuite.objects.Channel;
import com.minecraftdimensions.bungeesuite.objects.Messages;
import com.minecraftdimensions.bungeesuite.objects.ServerData;
import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatManager
{

	public static ArrayList<Channel> channels = new ArrayList<Channel>();
	public static HashMap<String, ServerData> serverData = new HashMap<String, ServerData>();
	public static boolean MuteAll;

	public static void loadChannels()
	{
		LoggingManager.log(ChatColor.GOLD + "Loading channels");
		Config chan = Channels.channelsConfig;
		loadChannel("Global", chan.getString("Channels.Global", Messages.CHANNEL_DEFAULT_GLOBAL));
		loadChannel("Admin", chan.getString("Channels.Admin", Messages.CHANNEL_DEFAULT_ADMIN));
		for (String servername : ProxyServer.getInstance().getServers().keySet()) 
		{
            loadServerData(servername, chan.getString("Channels.Servers." + servername + ".Shortname", servername.substring( 0, 1 )));
        }
	}
	
	private static void loadServerData(String name, String shortName) 
	{
        ServerData d = new ServerData(name, shortName);
        if (serverData.get(name) == null) 
        {
            serverData.put(name, d);
        }
    }

	public static void loadChannel(String name, String format)
	{
		Channel c = new Channel(name, format);
		channels.add(c);
	}

	public static void sendDefaultChannelsToServer(ServerInfo s)
	{
		ArrayList<Channel> chans = getDefaultChannels();
		for (Channel c : chans)
		{
			sendChannelToServer(s, c);
		}
	}

	public static void sendChannelToServer(ServerInfo server, Channel channel)
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("SendChannel");
			out.writeUTF(channel.serialise());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		sendPluginMessageTaskChat(server, b);
	}

	public static ArrayList<Channel> getDefaultChannels()
	{
		ArrayList<Channel> chans = new ArrayList<Channel>();
		for (Channel c : channels)
		{
			if (c.getName().equals("Global") || c.getName().equals("Admin"))
			{
				chans.add(c);
			}
		}
		return chans;
	}

	public static boolean channelExists(String name)
	{
		for (Channel c : channels)
		{
			if (c.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	public static Channel getChannel(String name)
	{
		for (Channel chan : channels)
		{
			if (chan.getName().equals(name))
			{
				return chan;
			}
		}
		return null;
	}

	public static Channel getSimilarChannel(String name)
	{
		for (Channel chan : channels)
		{
			if (chan.getName().toLowerCase().contains(name.toLowerCase()))
			{
				return chan;
			}
		}
		return null;
	}

	public static void sendPluginMessageTaskChat(ServerInfo server, ByteArrayOutputStream b)
	{
		BungeeSuite.proxy.getScheduler().runAsync(BungeeSuite.instance, new SendPluginMessage("BungeeSuiteChat", server, b));
	}

	public static void sendPlayer(String player, Server server, boolean serverConnect)
	{
		sendPlayer(PlayerManager.getPlayer(player), server.getInfo(), serverConnect);
	}
	
	public static void sendPlayer(BSPlayer player, ServerInfo server, boolean serverConnect)
	{
		if (serverConnect)
		{
			setPlayerToForcedChannel(player);
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("SendPlayer");
			out.writeUTF(player.getName());
			out.writeUTF(player.getChannel());
			out.writeBoolean(player.isMuted());
			out.writeUTF(player.getNickname());
			out.writeUTF(player.getUUID().toString());
			out.writeBoolean(player.isAFK());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		sendPluginMessageTaskChat(server, b);
	}
	
	public static void unloadPlayer(String player, ServerInfo server)
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("UnloadPlayer");
			out.writeUTF(player);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		sendPluginMessageTaskChat(server, b);
	}

	private static void setPlayerToForcedChannel(BSPlayer p)
	{
		setPlayersChannel(p, getChannel(ChatManager.getServersDefaultChannel()), false);
	}

	public static void setPlayerAFK(String player)
	{
		PlayerManager.setPlayerAFK(player);
	}

	public static void muteAll(String string)
	{
		if (MuteAll)
		{
			MuteAll = false;
			PlayerManager.sendBroadcast(Messages.MUTE_ALL_DISABLED.replace("{sender}", string));
		}
		else
		{
			MuteAll = true;
			PlayerManager.sendBroadcast(Messages.MUTE_ALL_ENABLED.replace("{sender}", string));
		}
	}

	public static void nickNamePlayer(String senderName, String targetName, String nickname, boolean on) throws SQLException
	{
		BSPlayer sender = PlayerManager.getPlayer(senderName);
		BSPlayer target;
		nickname = Utilities.colorize(nickname);
		if (nickname.length() > 50)
		{
			sender.sendMessage(Messages.NICKNAME_TOO_LONG);
			return;
		}
		if (!senderName.equals(targetName))
		{
			if (!PlayerManager.playerExists(targetName))
			{
				sender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST);
				return;
			}
			target = PlayerManager.getSimilarPlayer(targetName);
			if (target != null)
			{
				targetName = target.getName();
			}
		}
		else
		{
			target = sender;
		}
		if (!on)
		{
			PlayerManager.removeNickname(targetName);
			if (sender.getName().equals(targetName))
			{
				sender.sendMessage(Messages.NICKNAME_REMOVED);
			}
			else
			{
				sender.sendMessage(Messages.NICKNAME_REMOVED_PLAYER.replace("{player}", targetName));
				if (target != null)
				{
					target.sendMessage(Messages.NICKNAME_REMOVED);
				}
			}
			return;
		}
		if (PlayerManager.getSimilarNickPlayer(nickname) != null || PlayerManager.nickNameExists(nickname) || PlayerManager.playerExists(nickname) && !target.getName().equals(nickname))
		{
			sender.sendMessage(Messages.NICKNAME_TAKEN);
			return;
		}
		PlayerManager.setPlayersNickname(targetName, nickname);
		if (target != null && !target.equals(sender))
		{
			sender.sendMessage(Messages.NICKNAMED_PLAYER.replace("{player}", targetName).replace("{name}", nickname));
			target.sendMessage(Messages.NICKNAME_CHANGED.replace("{name}", nickname));
		}
		else
		{
			if (targetName.equals(sender.getName()))
			{
				sender.sendMessage(Messages.NICKNAME_CHANGED.replace("{name}", Utilities.colorize(nickname)));
			}
			else
			{
				sender.sendMessage(Messages.NICKNAMED_PLAYER.replace("{name}", Utilities.colorize(nickname)).replace("{player}", targetName));
			}
		}
	}

	public static void replyToPlayer(String sender, String message)
	{
		BSPlayer p = PlayerManager.getPlayer(sender);
		String reply = p.getReplyPlayer();
		if (p.isMuted() && ChatConfig.mutePrivateMessages)
		{
			p.sendMessage(Messages.MUTED);
			return;
		}
		if (reply == null)
		{
			p.sendMessage(Messages.NO_ONE_TO_REPLY);
			return;
		}
		PlayerManager.sendPrivateMessageToPlayer(p, reply, message);
	}

	public static void mutePlayer(String sender, String target, boolean command) throws SQLException
	{
		BSPlayer p = PlayerManager.getPlayer(sender);
		if (!PlayerManager.playerExists(target))
		{
			p.sendMessage(Messages.PLAYER_DOES_NOT_EXIST);
			return;
		}
		BSPlayer t = PlayerManager.getSimilarPlayer(target);
		if (t != null)
		{
			target = t.getName();
		}
		if (command)
		{
			command = !PlayerManager.isPlayerMuted(target);
		}
		else
		{
			if (!PlayerManager.isPlayerMuted(target))
			{
				p.sendMessage(Messages.PLAYER_NOT_MUTE);
				return;
			}
		}
		PlayerManager.mutePlayer(target);
		if (command)
		{
			p.sendMessage(Messages.PLAYER_MUTED.replace("{player}", target));
			return;
		}
		p.sendMessage(Messages.PLAYER_UNMUTED.replace("{player}", target));

	}

	public static void tempMutePlayer(String sender, String target, int minutes) throws SQLException
	{
		BSPlayer p = PlayerManager.getPlayer(sender);
		BSPlayer t = PlayerManager.getSimilarPlayer(target);
		if (t == null)
		{
			p.sendMessage(Messages.PLAYER_NOT_ONLINE);
			return;
		}
		PlayerManager.tempMutePlayer(t, minutes);
		p.sendMessage(Messages.PLAYER_MUTED.replace("{player}", t.getDisplayingName()));
	}

	public static void reloadChat()
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("Reload");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		for (ServerInfo s : BungeeSuite.proxy.getServers().values())
		{
			sendPluginMessageTaskChat(s, b);
		}
		channels.clear();
		serverData.clear();
		ChatConfig.reload();
		Channels.reload();
		loadChannels();
		for (ServerInfo s : BungeeSuite.proxy.getServers().values())
		{
			//ChatManager.sendServerData(s);
			ChatManager.sendDefaultChannelsToServer(s);
		}
		for (ProxiedPlayer p : BungeeSuite.proxy.getPlayers())
		{
			sendPlayer(p.getName(), p.getServer(), true);
		}
	}

	public static Channel getPlayersChannel(BSPlayer p)
	{
		return getChannel(p.getChannel());
	}

	public static Channel getPlayersNextChannel()
	{
		return getChannel("Global");
	}

	public static void setPlayersChannel(BSPlayer p, Channel channel, boolean message)
	{
		p.setChannel(channel.getName());
		p.updatePlayer();
		if (message)
		{
			p.sendMessage(Messages.CHANNEL_TOGGLE.replace("{channel}", channel.getName()));
		}
	}

	public static ServerData getServerData(Server server)
	{
		return serverData.get(server.getInfo().getName());
	}

	public static boolean canPlayerToggleToChannel()
	{
		return true;
	}

	public static void togglePlayersChannel(String player)
	{
		BSPlayer p = PlayerManager.getPlayer(player);
		setPlayersChannel(p, getPlayersNextChannel(), true);
	}

	public static void togglePlayerToChannel(String sender, String channel)
	{
		BSPlayer p = PlayerManager.getPlayer(sender);
		if (channel.equalsIgnoreCase("Global"))
		{
			channel = "Global";
		}
		else if (channel.equalsIgnoreCase("Admin"))
		{
			channel = "Admin";
		}
		else
		{
			throw new NullPointerException("Channel " + channel + " does not exist!");
		}
		
		Channel c = getSimilarChannel(channel);
		setPlayersChannel(p, c, true);
	}

	/*public static void sendServerData(ServerInfo s)
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("SendServerData");
			ServerData sd = serverData.get(s.getName());
			out.writeUTF(sd.getServerName());
			out.writeUTF(sd.getServerShortName());
			out.writeUTF(ChatConfig.globalChatRegex);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		sendPluginMessageTaskChat(s, b);
	}*/

	public static void sendGlobalChat(String player, String message, Server server)
	{
		if (ChatConfig.logChat)
		{
			LoggingManager.log(message);
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("SendGlobalChat");
			out.writeUTF(player);
			out.writeUTF(message);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		for (ServerInfo s : BungeeSuite.proxy.getServers().values())
		{
			if (!s.getName().equals(server.getInfo().getName()) && s.getPlayers().size() > 0)
			{
				sendPluginMessageTaskChat(s, b);
			}
		}
	}

	public static void sendAdminChat(String message, Server server)
	{
		if (ChatConfig.logChat)
		{
			LoggingManager.log(message);
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try
		{
			out.writeUTF("SendAdminChat");
			out.writeUTF(message);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		for (ServerInfo s : BungeeSuite.proxy.getServers().values())
		{
			if (!s.getName().equals(server.getInfo().getName()) && s.getPlayers().size() > 0)
			{
				sendPluginMessageTaskChat(s, b);
			}
		}
	}

	public static String getServersDefaultChannel()
	{
		return "Global";
	}
}
