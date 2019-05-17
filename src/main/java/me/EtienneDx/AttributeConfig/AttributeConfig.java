package me.EtienneDx.AttributeConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.bukkit.configuration.file.YamlConfiguration;

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
			String target = "";
			if(f.isAnnotationPresent(ConfigField.class))
			{
				ConfigField cf = f.getAnnotation(ConfigField.class);
				target = cf.name();
			}
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
		
		Class<? extends AttributeConfig> cls = getClass();
		
		for(Field f : cls.getFields())
		{
			String target = "";
			if(f.isAnnotationPresent(ConfigField.class))
			{
				ConfigField cf = f.getAnnotation(ConfigField.class);
				target = cf.name();
			}
			if(target.isEmpty())
				target = f.getName();
			
			try
			{
				config.set(target, f.get(this));
			} 
			catch (IllegalArgumentException e) { }
			catch (IllegalAccessException e) { }
		}
		
		try
        {
            config.save(path);
            return true;
        }
        catch (IOException exception) 
		{
        	return false;
		}
	}
}
