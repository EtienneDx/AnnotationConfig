package me.EtienneDx.AnnotationConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
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
				sb.append(s + "\n");
			}
			reader.close();
			strConfig = Pattern.compile("\\r?\\n? *?#[^\\r\\n]*").matcher(sb.toString()).replaceAll("");
			while(strConfig.startsWith("\n"))
				strConfig = strConfig.substring(1);
			strConfig = strConfig.replaceAll("\n+", "\n");
			
			/*PrintWriter minified = new PrintWriter(path + ".min.yml");
			minified.print(strConfig);
			minified.close();*/
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
	
	public boolean saveConfig()
	{
		return saveConfig(this.path);
	}
	
	public boolean saveConfig(String path)
	{
		if(path == null || path.isEmpty())
			path = "config.yml";
		this.path = path;
		
		YamlConfiguration config = new YamlConfiguration();
		
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
						config.set(target + "_COMMENT", cf.comment().replaceAll(":", "{{{REPLACE_ME_WITH_DOUBLE_DOTS}}}"));
					}
				
					config.set(target, f.get(this));
				} 
				catch (IllegalArgumentException e) { return false; }
				catch (IllegalAccessException e) { return false; }
			}
		}
		
		try
        {
			String configString = config.saveToString();//.save(path);
			
			if(getClass().isAnnotationPresent(ConfigFile.class))
			{
				String header = getClass().getAnnotation(ConfigFile.class).header();
				String headerS = "";
				if(!header.isEmpty())
				{
					for(String s : header.split("\n"))
						headerS += "# " + s + "\n";
				}
				configString = headerS + configString;
			}
            
            Matcher matcher = Pattern.compile("(?:[A-Za-z0-9]*?)_COMMENT: ?(.*?)(\\n[^:\\n]*?:)", Pattern.DOTALL).matcher(configString);
            
            StringBuffer newConfig = new StringBuffer();
            
            while(matcher.find())
            {
            	String comm = matcher.group(1);
            	comm = "# " + Pattern.compile("\n( *)").matcher(comm).replaceAll("\n$1# ").replaceAll("\\{\\{\\{REPLACE_ME_WITH_DOUBLE_DOTS\\}\\}\\}", ":");
            	comm += matcher.group(2);
            	matcher.appendReplacement(newConfig, Matcher.quoteReplacement(comm));
            }
            matcher.appendTail(newConfig);
            configString = newConfig.toString();
            
            File f = new File(path).getParentFile();
            
            if(f != null)
            	f.mkdirs();
            
            FileWriter file = new FileWriter(path);
            		
            file.write(configString);
            
            file.close();
            return true;
        }
        catch (IOException exception) 
		{
        	return false;
		}
	}
}
