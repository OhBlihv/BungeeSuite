package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import com.minecraftdimensions.bungeesuite.configs.MainConfig;
import com.minecraftdimensions.bungeesuite.objects.ConnectionHandler;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class SQLManager
{

	private static ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();

	public static boolean initialiseConnections()
	{
		Connection connection = null;
		for (int i = 0; i < MainConfig.threads; i++)
		{
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://" + MainConfig.host + ":" + MainConfig.port + "/" + MainConfig.database, MainConfig.username, MainConfig.password);
			}
			catch (SQLException | ClassNotFoundException ex)
			{
				System.out.println(ChatColor.DARK_RED + "SQL is unable to conect");
				return false;
			}
			connections.add(new ConnectionHandler(connection));
			connection = null;
		}
		
		ProxyServer.getInstance().getScheduler().schedule(BungeeSuite.instance, new Runnable()
		{
			public void run()
			{
				ProxyServer.getInstance().getScheduler().runAsync(BungeeSuite.instance, new Runnable()
				{

					@Override
					public void run()
					{
						Iterator<ConnectionHandler> cons = connections.iterator();
						while (cons.hasNext())
						{
							ConnectionHandler con = cons.next();
							if (con.isOldConnection())
							{
								con.closeConnection();
								cons.remove();
							}
						}
					}

				});
			}
		}, 60, 60, TimeUnit.MINUTES);
		return true;
	}

	/**
	 * @return Returns a free connection from the pool of connections. Creates a
	 *         new connection if there are none available
	 */
	public static ConnectionHandler getConnection()
	{
		for (ConnectionHandler c : connections)
		{
			if (!c.isUsed())
			{
				return c;
			}
		}
		
		// create a new connection as none are free
		Connection connection = null;
		ConnectionHandler ch = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + MainConfig.host + ":" + MainConfig.port + "/" + MainConfig.database, MainConfig.username, MainConfig.password);
		}
		catch (SQLException | ClassNotFoundException ex)
		{
			System.out.println("SQL is unable to create a new connection");
		}
		ch = new ConnectionHandler(connection);
		connections.add(ch);
		System.out.println("Created new sql connection!");

		return ch;

	}

	/**
	 * Any query which does not return a ResultSet object. Such as : INSERT,
	 * UPDATE, CREATE TABLE...
	 *
	 * @param query
	 * @throws SQLException
	 */
	public static void standardQuery(String query) throws SQLException
	{
		ConnectionHandler ch = getConnection();
		standardQuery(query, ch.getConnection());
		ch.release();
	}

	/**
	 * Check whether a field/entry exists in a database.
	 *
	 * @param query
	 * @return Whether or not a result has been found in the query.
	 * @throws SQLException
	 */
	public static boolean existanceQuery(String query)
	{
		boolean check = false;
		ConnectionHandler ch = getConnection();
		try
		{
			check = sqlQuery(query, ch.getConnection()).next();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		ch.release();
		return check;
	}

	/**
	 * Any query which returns a ResultSet object. Such as : SELECT Remember to
	 * close the ResultSet object after you are done with it to free up
	 * resources immediately. ----- ResultSet set =
	 * sqlQuery("SELECT * FROM sometable;"); set.doSomething(); set.close();
	 * -----
	 *
	 * @param query
	 * @return ResultSet
	 */
	public static ResultSet sqlQuery(String query)
	{
		ResultSet res = null;
		ConnectionHandler ch = getConnection();
		res = sqlQuery(query, ch.getConnection());
		ch.release();
		return res;
	}

	/**
	 * Check whether the table name exists.
	 *
	 * @param table
	 * @return
	 */
	public static boolean doesTableExist(String table)
	{
		boolean check = false;
		ConnectionHandler ch = getConnection();
		check = checkTable(table, ch.getConnection());
		ch.release();
		return check;
	}

	protected synchronized static void standardQuery(final String query, final Connection connection)
	{

		ProxyServer.getInstance().getScheduler().runAsync(BungeeSuite.instance, new Runnable()
		{

			@Override
			public void run()
			{
				Statement statement = null;

				try
				{
					statement = connection.createStatement();

					statement.executeUpdate(query);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				finally
				{
					if(statement != null)
					{
						try
						{
							statement.close();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				}
			}

		});

	}

	protected synchronized static ResultSet sqlQuery(String query, Connection connection)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			try
			{
				return statement.executeQuery(query);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	protected synchronized static boolean checkTable(String table, Connection connection)
	{
		DatabaseMetaData dbm = null;
		try
		{
			dbm = connection.getMetaData();
			ResultSet tables = null;
			tables = dbm.getTables(null, null, table, null);
			return tables.next();
		}
		catch (SQLException e2)
		{
			e2.printStackTrace();
		}
		return false;
	}

	public static void closeConnections()
	{
		for (ConnectionHandler c : connections)
		{
			c.closeConnection();
		}

	}
}
