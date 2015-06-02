package com.minecraftdimensions.bungeesuite.listeners;

import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
import com.minecraftdimensions.bungeesuite.managers.*;
import com.minecraftdimensions.bungeesuite.objects.BSPlayer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class ChatMessageListener implements Listener
{

	@EventHandler
	public void receivePluginMessage(PluginMessageEvent event) throws IOException, SQLException
	{
		if (event.isCancelled() || !(event.getSender() instanceof Server) || (!event.getTag().equalsIgnoreCase("BSChat")))
		{
			return;
		}
		event.setCancelled(true);
		Server s = (Server) event.getSender();
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
		String task = in.readUTF();

		if (task.equals("Broadcast"))
		{
			String msg = in.readUTF();
			ProxyServer.getInstance().broadcast(new TextComponent(msg));
			return;
		}
		else if (task.equals("LogChat"))
		{
			String message = in.readUTF();
			if (ChatConfig.logChat)
			{
				LoggingManager.log(message);
			}
			PlayerManager.sendMessageToSpies(s, message);
		}
		else if (task.equals("GlobalChat"))
		{
			String sender = in.readUTF();
			String message = in.readUTF();
			ChatManager.sendGlobalChat(sender, message, s);
		}
		else if (task.equals("RealName"))
		{
			String name = in.readUTF();
			String nick = in.readUTF();
			BSPlayer p = PlayerManager.getSimilarNickPlayer(nick);
			if (p == null)
			{
				PlayerManager.sendMessageToPlayer(name, ChatColor.GRAY + nick + ChatColor.RESET + ChatColor.GRAY + " was not found!");
			}
			else
			{
				PlayerManager.sendMessageToPlayer(name, ChatColor.WHITE + p.getNickname() + ChatColor.RESET + ChatColor.GRAY + " is " + p.getName());
			}
		}
		/*else if (task.equals("GetServerChannels"))
		{
			ChatManager.sendServerData(s.getInfo());
			ChatManager.sendDefaultChannelsToServer(s.getInfo());
			PrefixSuffixManager.sendPrefixAndSuffixToServer(s.getInfo());
		}*/
		else if (task.equals("AdminChat"))
		{
			String message = in.readUTF();
			ChatManager.sendAdminChat(message, (Server) event.getSender());
		}
		else if (task.equals("GetPlayer"))
		{
			String player = in.readUTF();
			ChatManager.sendPlayer(player, s, true);
			IgnoresManager.sendPlayersIgnores(PlayerManager.getPlayer(player), s);
		}
		else if (task.equals("AFKPlayer"))
		{
			ChatManager.setPlayerAFK(in.readUTF());
		}
		else if (task.equals("ReplyToPlayer"))
		{
			ChatManager.replyToPlayer(in.readUTF(), in.readUTF());
		}
		else if (task.equals("PrivateMessage"))
		{
			BSPlayer p = PlayerManager.getPlayer(in.readUTF());
			PlayerManager.sendPrivateMessageToPlayer(p, in.readUTF(), in.readUTF());
		}
		else if (task.equals("IgnorePlayer"))
		{
			BSPlayer p = PlayerManager.getPlayer(in.readUTF());
			IgnoresManager.addIgnore(p, in.readUTF());
		}
		else if (task.equals("UnIgnorePlayer"))
		{
			BSPlayer p = PlayerManager.getPlayer(in.readUTF());
			IgnoresManager.removeIgnore(p, in.readUTF());
		}
		else if (task.equals("MuteAll"))
		{
			ChatManager.muteAll(in.readUTF());
		}
		else if (task.equals("MutePlayer"))
		{
			ChatManager.mutePlayer(in.readUTF(), in.readUTF(), in.readBoolean());
		}
		else if (task.equals("NickNamePlayer"))
		{
			ChatManager.nickNamePlayer(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean());
		}
		else if (task.equals("TempMutePlayer"))
		{
			ChatManager.tempMutePlayer(in.readUTF(), in.readUTF(), in.readInt());
		}
		else if (task.equals("ReloadChat"))
		{
			ChatManager.reloadChat();
		}
		else if (task.equals("TogglePlayersChannel"))
		{
			ChatManager.togglePlayersChannel(in.readUTF());
		}
		else if (task.equals("TogglePlayerToChannel"))
		{
			ChatManager.togglePlayerToChannel(in.readUTF(), in.readUTF());
		}
	}

}
