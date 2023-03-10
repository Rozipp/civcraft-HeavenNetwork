package com.avrgaming.civcraft.command.debug;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.ChunkCoord;

public class DebugWorldCommand extends CommandBase {

	@Override
	public void init() {
		command = "/dbg world";
		displayName = "Debug World";
		
		cs.add("create", "[name] - creates a new test world with this name.");
		cs.add("tp", "[name] teleports you to spawn at the specified world.");
		cs.add("list", "Lists worlds according to bukkit.");
	}
	
	public void list_cmd() {
		CivMessage.sendHeading(sender, "Worlds");
		for (World world : Bukkit.getWorlds()) {
			CivMessage.send(sender, world.getName());
		}
	}
	
	public void create_cmd() throws CivException {
		String name = getNamedString(1, "enter a world name");
		
		WorldCreator wc = new WorldCreator(name);
		wc.environment(Environment.NORMAL);
		wc.type(WorldType.FLAT);
		wc.generateStructures(false);
		
		World world = Bukkit.getServer().createWorld(wc);
		world.setSpawnFlags(false, false);
		ChunkCoord.addWorld(world);
		
		CivMessage.sendSuccess(sender, "World "+name+" created.");
		
	}
	
	public void tp_cmd() throws CivException {
		String name = getNamedString(1, "enter a world name");
		Player player = getPlayer();
		
		World world = Bukkit.getWorld(name);
		player.teleport(world.getSpawnLocation());
		
		CivMessage.sendSuccess(sender, "Teleported to spawn at world:"+name);
	}
	

	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		
	}

}
