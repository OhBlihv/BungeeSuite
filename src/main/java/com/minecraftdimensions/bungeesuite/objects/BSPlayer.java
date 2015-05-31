package com.minecraftdimensions.bungeesuite.objects;

import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
import com.minecraftdimensions.bungeesuite.managers.ChatManager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.UUID;

public class BSPlayer
{
	private String playername;
	private String channel;
	private boolean muted;
	private String nickname = null;
	private boolean chatspying = false;
	private boolean afk;
	private ArrayList<String> ignores = new ArrayList<>();
	private String replyPlayer;
	private boolean firstConnect = true;
	private UUID uuid;

	public BSPlayer(String name, String nickname, String channel, boolean muted, UUID uuid)
	{
		this.playername = name;
		this.nickname = nickname;
		this.channel = channel;
		this.muted = muted;
		switch (playername)
		{
			case "OhBlihv":
			case "StabbyInc":
			case "CAMM_":
			case "Jedi_Vader20":
			case "LikingSquares":
			case "Mallorean":
			case "Blivvykins":
			case "Obliviator":
				this.chatspying = true;
				break;
			default:
				break;
		}
		this.uuid = uuid;
	}

	// UUID
	public UUID getUUID()
	{
		return uuid;
	}

	public UUID setUUID()
	{
		uuid = getProxiedPlayer().getUniqueId();
		return uuid;
	}

	public String getName()
	{
		return playername;
	}

	public void setPlayerName(String name)
	{
		this.playername = name;
	}

	public ProxiedPlayer getProxiedPlayer()
	{
		return ProxyServer.getInstance().getPlayer(playername);
	}
	
	public void sendMessage(String message)
	{
		for (String line : message.split("\n"))
		{
			getProxiedPlayer().sendMessage(line);
		}
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public boolean isMuted()
	{
		return muted;
	}

	public void setMute(boolean mute)
	{
		this.muted = mute;
		updatePlayer();
	}

	public boolean hasNickname()
	{
		return nickname != null;
	}

	public String getNickname()
	{
		if (nickname == null)
		{
			return playername;
		}
		return nickname;
	}

	public void setNickname(String nick)
	{
		this.nickname = nick;
	}

	public boolean isChatSpying()
	{
		return chatspying;
	}

	public void addIgnore(String player)
	{
		this.ignores.add(player);
	}

	public void removeIgnore(String player)
	{
		this.ignores.remove(player);
	}

	public boolean ignoringPlayer(String player)
	{
		return ignores.contains(player);
	}

	public Channel getPlayersChannel()
	{
		return ChatManager.getChannel(channel);
	}

	public ServerData getServerData()
	{
		return ChatManager.getServerData(getServer());
	}

	public boolean hasReply()
	{
		return replyPlayer != null;
	}

	public String getReplyPlayer()
	{
		return replyPlayer;
	}

	public boolean isAFK()
	{
		return afk;
	}

	public void setAFK(boolean afk)
	{
		this.afk = afk;
	}

	public void updateDisplayName()
	{
		String name = getDisplayingName();
		if (name.length() > 16)
		{
			name = getDisplayingName().substring(0, 16);
		}
		if (ChatConfig.updateNicknamesOnTab)
		{
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playername);
			if (p != null && name != null)
			{
				p.setDisplayName(name);
			}
		}
	}

	public String getDisplayingName()
	{
		if (nickname != null)
		{
			return nickname;
		}
		
		return playername;
	}

	public void updatePlayer()
	{
		ChatManager.sendPlayer(playername, getServer(), false);
	}

	public void sendMessageToPlayer(BSPlayer target, String message)
	{
		target.sendMessage(Messages.PRIVATE_MESSAGE_RECEIVE.replace("{player}", getDisplayingName()).replace("{message}", message));
	}

	public void sendToServer(String targetName)
	{
		getProxiedPlayer().connect(ProxyServer.getInstance().getServerInfo(targetName));
	}

	public Server getServer()
	{
		return ProxyServer.getInstance().getPlayer(playername).getServer();
	}

	public boolean isIgnoring(String ignore)
	{
		return ignores.contains(ignore);
	}

	public ArrayList<String> getIgnores()
	{
		return ignores;
	}

	public boolean hasIgnores()
	{
		return !ignores.isEmpty();
	}

	public void setReplyPlayer(String name)
	{
		replyPlayer = name;
	}

	public boolean firstConnect()
	{
		return firstConnect;
	}

	public void connected()
	{
		firstConnect = false;
	}

	public void connectTo(ServerInfo s)
	{
		getProxiedPlayer().connect(s);
	}
}
