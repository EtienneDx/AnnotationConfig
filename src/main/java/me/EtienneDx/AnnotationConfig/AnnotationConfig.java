package me.EtienneDx.AnnotationConfig;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class AnnotationConfig
{
	private String path = null;
	
	public void loadConfig()
	{
		loadConfig(this.path);
	}
	
	public void loadConfig(String path)
	{
		if(path == null || path.isEmpty())
			path = "config.yml";
		this.path = path;
		String strConfig = "";
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String s;
			StringBuilder sb = new StringBuilder();
			while((s = reader.readLine()) != null)
			{
				sb.append(s);
			}
			reader.close();
			strConfig = Pattern.compile("\n?#(.*?)\n?").matcher(sb.toString()).replaceAll("\n");
		}
		catch (FileNotFoundException e) { }
		catch (IOException e) { }
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.loadFromString(strConfig);
		}
		catch (InvalidConfigurationException e1) { }
		
		Class<? extends AnnotationConfig> cls = getClass();
		
		for(Field f : cls.getFields())
		{
			if(f.isAnnotationPresent(ConfigField.class))
			{
				String target = "";
				ConfigField cf = f.getAnnotation(ConfigField.class);
				target = cf.name();
				if(target.isEmpty())
					target = f.getName();
				
				try
				{
					f.set(this, config.get(target, f.get(this)));
				} 
				catch (IllegalArgumentException e) { }
				catch (IllegalAccessException e) { }
			}
		}
	}
	
	public void saveConfig()
	{
		saveConfig(this.path);
	}
	
	public boolean saveConfig(String path)
	{
		if(path == null || path.isEmpty())
			path = "config.yml";
		this.path = path;
		
		YamlConfiguration config = new YamlConfiguration();
		
		HashMap<String, String> comments = new HashMap<String, String>();
		
		Class<? extends AnnotationConfig> cls = getClass();
		
		for(Field f : cls.getFields())
		{
			if(f.isAnnotationPresent(ConfigField.class))
			{
				String target = "";
				ConfigField cf = f.getAnnotation(ConfigField.class);
				target = cf.name();
				if(target.isEmpty())
					target = f.getName();
				try
				{
					if(!cf.comment().isEmpty())
					{
						comments.put(target, cf.comment());
						config.set(target + "_COMMENT", cf.comment());
					}
				
					config.set(target, f.get(this));
				} 
				catch (IllegalArgumentException e) { }
				catch (IllegalAccessException e) { }
			}
		}
		
		try
        {
			/*FileWriter file = new FileWriter(path);
			if(getClass().isAnnotationPresent(ConfigFile.class))
			{
				String header = getClass().getAnnotation(ConfigFile.class).header();
				if(!header.isEmpty())
					file.write("# " + header.replace("\n", "\n# ") + "\n");
			}
			for(String s : config.getKeys(false))
			{
				if(config.isConfigurationSection(s))
				{
					if(!writeSection(config.getConfigurationSection(s), file, comments, 0));
						return false;
				}
				else
				{
					if(!writeField(s, config.get(s), file, comments, 0))
						return false;
				}
			}
			
            file.close();*/
			
            String configString = config.saveToString();//.save(path);
            
            FileWriter file = new FileWriter(path);
            
            configString = Pattern.compile("^ *?(?:.*?)_COMMENT: ?(.*?)$", Pattern.MULTILINE).matcher(configString).replaceAll("# $1");
            //configString = configString.replaceAll("^ *?(?:.*?)_COMMENT: ?(.*?)$", "# $1");
            file.write(configString);
            
            file.close();
            return true;
        }
        catch (IOException exception) 
		{
        	return false;
		}
	}

	/*private String getIndented(String s, int tabCount)
	{
		String tb = "";
		for(int i = 0; i < tabCount; i++)
			tb += "  ";
		s = tb + s.replace("\r?\n", "\n" + tb);
		return s;
	}*/
}