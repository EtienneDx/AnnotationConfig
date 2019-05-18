package me.EtienneDx.AttributeConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

public class AttributeConfig
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
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(path));
		
		Class<? extends AttributeConfig> cls = getClass();
		
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
		
		Class<? extends AttributeConfig> cls = getClass();
		
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

	private boolean writeField(String key, Object object, FileWriter file, HashMap<String, String> comments, int tabCount)
	{
		if(!writeTabs(file, tabCount))
			return false;
		try
		{
			if(comments.containsKey(key))
			{
				String comment = "# " + comments.get(key);
				file.append(comment + "\n");
			}
			
			String s = (new Yaml()).dump(object);
			if(s.contains("\n"))
			{
				s = "\n" + getIndented(s, tabCount);
			}
			String[] str = key.split("\\.");
			file.append(getIndented(str[str.length - 1], tabCount) + s);
		}
		catch (IOException e)
		{
			return false;
		}
		
		return true;
	}

	private String getIndented(String s, int tabCount)
	{
		String tb = "";
		for(int i = 0; i < tabCount; i++)
			tb += "  ";
		s = tb + s.replace("\r?\n", "\n" + tb);
		return s;
	}

	private boolean writeSection(ConfigurationSection cfgSection, FileWriter file, HashMap<String,String> comments, int tabCount)
	{
		if(!writeTabs(file, tabCount))
			return false;
		try
		{
			file.append(cfgSection.getName() + ":");
		}
		catch (IOException e)
		{
			return false;
		}
		for(String s : cfgSection.getKeys(false))
		{
			if(cfgSection.isConfigurationSection(s))
			{	
				if(!writeSection(cfgSection.getConfigurationSection(s), file, comments, tabCount + 1))
					return false;
			}
			else
			{
				if(!writeField(s, cfgSection.get(s), file, comments, tabCount + 1))
					return false;
			}
		}
		return true;
	}

	private boolean writeTabs(FileWriter file, int tabCount)
	{
		for(int i = 0; i < tabCount; i++)
		{
			try
			{
				file.append("  ");// use double space as tab
			}
			catch (IOException e)
			{
				return false;
			}
		}
		return true;
	}
}
