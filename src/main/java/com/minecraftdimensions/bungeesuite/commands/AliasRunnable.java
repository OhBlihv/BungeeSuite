package com.minecraftdimensions.bungeesuite.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import org.json.JSONArray;
import org.json.JSONObject;

import com.minecraftdimensions.bungeesuite.managers.PlayerManager;

public class AliasRunnable implements Runnable {
	
	String playerName;
	String uuid;
	
	public AliasRunnable(String inPlayer, String inUUID)
	{
		playerName = inPlayer;
		uuid = inUUID;
	}
	
	@Override
	public void run()
	{
		String id = uuid.toString().replace("-", "");
        String urlString = String.format("https://api.mojang.com/user/profiles/%s/names", new Object[] {id});
    	
    	String jsonString = "";
    	URL url = null;
    	try
    	{
    		url = new URL(urlString);
	        URLConnection conn = url.openConnection();
	        conn.setConnectTimeout(5000);
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        
	        for(String line = null; (line = in.readLine()) != null;)
	            jsonString = jsonString + line;

	        if(jsonString.isEmpty())
	        {
	            return;
	        }
    	}
    	catch(Throwable e)
    	{
    		e.printStackTrace();
    	}
    	
        
        try
        {
            JSONArray nameArray = new JSONArray(jsonString);
            List<String> names = new ArrayList<String>();
            for(int i = 0; i < nameArray.length(); i++)
            {
                JSONObject obj = nameArray.getJSONObject(i);
                if(obj != null)
                {
                    names.add(obj.getString("name"));
                }
            }
            
            String aliasString = "";
            for(String name : names)
			{
				aliasString += name + ", ";
			}
            
            TextComponent msg = new TextComponent("Aliases: ");
			TextComponent aliasText = new TextComponent(aliasString);
			aliasText.setColor(ChatColor.GREEN);
			msg.setColor(ChatColor.AQUA);
			msg.addExtra(aliasText);
            
            //PlayerManager.getPlayer(playerName).sendMessage(msg);
			PlayerManager.sendBroadcast(msg.toPlainText());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return;
		
	}

}
