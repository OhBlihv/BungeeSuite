package com.minecraftdimensions.bungeesuite.managers;

import java.sql.SQLException;

import com.minecraftdimensions.bungeesuite.BungeeSuite;

import net.md_5.bungee.api.ProxyServer;

public class DatabaseTableManager
{

	public static void runTableQuery(final String name, final String query)
	{
		ProxyServer.getInstance().getScheduler().runAsync(BungeeSuite.instance, new Runnable()
		{
			public void run()
			{
				if (!SQLManager.doesTableExist(name))
				{
					try
					{
						SQLManager.standardQuery(query);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
			
		});
		
	}

	public static void createDefaultTables()
	{
		runTableQuery(
				"BungeePlayers",
				"CREATE TABLE BungeePlayers (UUID VARCHAR(36) PRIMARY KEY, playername VARCHAR(16), lastonline DATETIME NOT NULL, ipaddress VARCHAR(100), nickname VARCHAR(50),"
				+ "muted TINYINT(1) DEFAULT 0);");
		runTableQuery(
				"BungeeChatIgnores",
				"CREATE TABLE BungeeChatIgnores (player VARCHAR(100), ignoring VARCHAR(100), CONSTRAINT pk_ignored PRIMARY KEY (player,ignoring),"
				+ " CONSTRAINT fk_player FOREIGN KEY (player) REFERENCES BungeePlayers (playername) ON UPDATE CASCADE ON DELETE CASCADE,"
				+ " CONSTRAINT fk_ignored FOREIGN KEY (ignoring) REFERENCES BungeePlayers (playername) ON UPDATE CASCADE ON DELETE CASCADE)");
	}
}
