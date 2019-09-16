/************************************************************************* AVRGAMING LLC __________________
 * 
 * [2013] AVRGAMING LLC All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property of AVRGAMING LLC and its suppliers, if any. The intellectual and technical concepts
 * contained herein are proprietary to AVRGAMING LLC and its suppliers and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is
 * obtained from AVRGAMING LLC. */
package com.avrgaming.civcraft.units;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMission;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.MissionLogger;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.Capitol;
import com.avrgaming.civcraft.structure.Cottage;
import com.avrgaming.civcraft.structure.FishingBoat;
import com.avrgaming.civcraft.structure.Granary;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.structure.TradeOutpost;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.units.EquipmentElement.Equipments;
import com.avrgaming.civcraft.util.BookUtil;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public class Spy extends UnitMaterial {

	public final int BOOK_ID = 403;

	public Spy(String id, ConfigUnit configUnit) {
		super(id, configUnit);
	}

	@Override
	public void initUnitObject(UnitObject uo) {
		uo.setComponent("sword", 0);
	}

	@Override
	public void initLore(AttributeUtil attrs, UnitObject uo) {
		Integer temp;
		temp = uo.getComponent(Equipments.HELMET);
		String h = CivColor.Green + ((temp != 0) ? "T" + temp : "--");
		temp = uo.getComponent(Equipments.CHESTPLATE);
		String c = CivColor.Green + ((temp != 0) ? "T" + temp : "--");
		temp = uo.getComponent(Equipments.LEGGINGS);
		String l = CivColor.Green + ((temp != 0) ? "T" + temp : "--");
		temp = uo.getComponent(Equipments.BOOTS);
		String b = CivColor.Green + ((temp != 0) ? "T" + temp : "--");
		temp = uo.getComponent(Equipments.SWORD);
		String s = CivColor.Green + ((temp != 0) ? "T" + temp : "--");
		temp = uo.getComponent(Equipments.TWOHAND);
		String t = CivColor.Green + ((temp != 0) ? "T" + temp : "--");
		
		attrs.addLore(CivColor.RoseBold + "           Амуниция:");
		attrs.addLore(CivColor.Rose     + "          Голова  : " + h);
		attrs.addLore(CivColor.Rose     + "  мечь    Грудь   : " + c + CivColor.Rose + "    лук");
		attrs.addLore(CivColor.Rose     + "   " + s + CivColor.Rose + "     Ноги    : " + l + CivColor.Rose + "     " + t);
		attrs.addLore(CivColor.Rose     + "          Ступни  : " + b);
	}
	@Override
	public void initAmmunitions() {
		EquipmentElement e = null;
		//helmet
		e = new EquipmentElement(39);
		e.addMatTir("");
		e.addMatTir("mat_leather_helmet");
		e.addMatTir("mat_refined_leather_helmet");
		e.addMatTir("mat_hardened_leather_helmet");
		e.addMatTir("mat_composite_leather_helmet");
		equipmentElemens.put(Equipments.HELMET, e);

		//chestplate
		e = new EquipmentElement(38);
		e.addMatTir("");
		e.addMatTir("mat_leather_chestplate");
		e.addMatTir("mat_refined_leather_chestplate");
		e.addMatTir("mat_hardened_leather_chestplate");
		e.addMatTir("mat_composite_leather_chestplate");
		equipmentElemens.put(Equipments.CHESTPLATE, e);

		//leggings
		e = new EquipmentElement(37);
		e.addMatTir("");
		e.addMatTir("mat_leather_leggings");
		e.addMatTir("mat_refined_leather_leggings");
		e.addMatTir("mat_hardened_leather_leggings");
		e.addMatTir("mat_composite_leather_leggings");
		equipmentElemens.put(Equipments.LEGGINGS, e);

		//boots
		e = new EquipmentElement(36);
		e.addMatTir("");
		e.addMatTir("mat_leather_boots");
		e.addMatTir("mat_refined_leather_boots");
		e.addMatTir("mat_hardened_leather_boots");
		e.addMatTir("mat_composite_leather_boots");
		equipmentElemens.put(Equipments.BOOTS, e);

		//sword
		e = new EquipmentElement(0);
		e.addMatTir("mat_stone_sword");
		e.addMatTir("mat_iron_sword");
		e.addMatTir("mat_steel_sword");
		e.addMatTir("mat_carbide_steel_sword");
		e.addMatTir("mat_tungsten_sword");
		equipmentElemens.put(Equipments.SWORD, e);

		//two
		e = new EquipmentElement(40);
		e.addMatTir("");
		e.addMatTir("mat_hunting_bow");
		e.addMatTir("mat_recurve_bow");
		e.addMatTir("mat_longbow");
		e.addMatTir("mat_marksmen_bow");
		equipmentElemens.put(Equipments.TWOHAND, e);
	}


	@Override
	public void onItemToPlayer(Player player, ItemStack stack) {
	}

	@Override
	public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {

		Player player = (Player) event.getEntity();
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) return;

		ArrayList<String> bookout = MissionLogger.getMissionLogs(resident.getTown());

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) book.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Mission Report");
		meta.setAuthor("Mission Reports");
		meta.setTitle("Missions From" + " " + resident.getTown().getName());

		String out = "";
		for (String str : bookout) {
			out += str + "\n";
		}
		BookUtil.paginate(meta, out);

		meta.setLore(lore);
		book.setItemMeta(meta);
		player.getWorld().dropItem(player.getLocation(), book);

	}

	public static double getMissionFailChance(ConfigMission mission, Town town) {
		int onlineResidents = town.getOnlineResidents().size();
		double chance = 1 - mission.fail_chance;
		if (mission.intel != 0) {
			double percentIntel = (double) onlineResidents / (double) mission.intel;
			if (percentIntel > 1.0) percentIntel = 1.0;
			chance *= percentIntel;
		}
		chance = 1 - chance; /* Convert to failure chance */
		return chance;
	}

	public static double getMissionCompromiseChance(ConfigMission mission, Town town) {
		int onlineResidents = town.getOnlineResidents().size();
		double chance = 1 - mission.compromise_chance;
		if (mission.intel != 0) {
			double percentIntel = (double) onlineResidents / (double) mission.intel;
			if (percentIntel > 1.0) percentIntel = 1.0;
			chance *= percentIntel;
		}
		chance = 1 - chance; /* convert to failure chance */
		return chance;
	}
	
	public static void performMission(ConfigMission mission, String playerName) {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e1) {
			return;
		}

		try {
			Resident resident = CivGlobal.getResident(playerName);
			if (!resident.getTown().getTreasury().hasEnough(mission.cost)) {
				throw new CivException(CivSettings.localize.localizedString("var_missionBook_errorTooPoor",mission.cost,CivSettings.CURRENCY_NAME));
			}
			
			switch (mission.id) {
			case "spy_investigate_town":
				performInvestigateTown(player, mission);
				break;
			case "spy_steal_treasury":
				performStealTreasury(player, mission);
				break;
			case "spy_incite_riots":
				performInciteRiots(player, mission);
				break;
			case "spy_poison_granary":
				performPosionGranary(player, mission);
				break;
			case "spy_pirate":
				performPirate(player, mission);
				break;
			case "spy_sabotage":
				performSabotage(player, mission);
				break;
			case "spy_subvert_government":
				performSubertGov(player, mission);
				break;
			}
			
		} catch (CivException e) {
			CivMessage.sendError(player, e.getMessage());
		}
	}
	
	private static boolean processMissionResult(Player player, Town target, ConfigMission mission) {
		return processMissionResult(player, target, mission, 1.0, 1.0);
	}
	private static boolean processMissionResult(Player player, Town target, ConfigMission mission, double failModifier, 
			double compromiseModifier) {
		
		int fail_rate = (int)((Spy.getMissionFailChance(mission, target)*failModifier)*100);
		int compromise_rate = (int)((Spy.getMissionCompromiseChance(mission, target)*compromiseModifier)*100);
		Resident resident = CivGlobal.getResident(player);

		if (resident == null || !resident.hasTown()) {
			return false;
		}
		
		if (!resident.getTown().getTreasury().hasEnough(mission.cost)) {
			CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("missionBook_errorTownBroke"));
			return false;
		}
		
		resident.getTown().getTreasury().withdraw(mission.cost);
		
		Random rand = new Random();
		String result = "";
		int failnext = rand.nextInt(100);
		if (failnext < fail_rate) {
			int next = rand.nextInt(100);
			result += "Failed";
			
			if (next < compromise_rate) {
				CivMessage.global(CivColor.Yellow+CivSettings.localize.localizedString("missionBook_caughtHeading")+CivColor.White+" "+
						CivSettings.localize.localizedString("var_missionBook_caughtmsg1",player.getName(),mission.name,target.getName()));
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("missionBook_caughtAlert1")+" ("+CivSettings.localize.localizedString("missionBook_caughtRolled")+" "+next+" vs "+compromise_rate+") "+CivSettings.localize.localizedString("missionBook_spyDestroyed"));
				UnitStatic.removeUnit(player, "u_settler");
				result += ", "+CivSettings.localize.localizedString("missionBook_spyCompromised");
			}
			
			MissionLogger.logMission(resident.getTown(), target, resident, mission.name, result);
			CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("missionBook_missionFailed")+" ("+CivSettings.localize.localizedString("missionBook_caughtRolled")+" "+failnext+" vs "+fail_rate+")");
			return false;
		}
		
		MissionLogger.logMission(resident.getTown(), target, resident, mission.name, CivSettings.localize.localizedString("missionBook_success"));
		return true;

	}
	
	
	private static void performSabotage(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc == null || cc.getCiv() == resident.getTown().getCiv()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorBorder"));
		}
		
		// Check that the player is within range of the town hall.
		Buildable buildable = cc.getTown().getNearestBuildable(player.getLocation());
		if (buildable instanceof TownHall) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_sabatoge_errorTownHall"));
		}
		if (buildable instanceof Wonder) {
			if (buildable.isComplete()) {
				throw new CivException(CivSettings.localize.localizedString("missionBook_sabatoge_errorCompleteWonder"));
			}
		}
		
		double distance = player.getLocation().distance(buildable.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException(CivSettings.localize.localizedString("var_missionBook_sabatoge_errorTooFar",buildable.getDisplayName()));
		}
		
		if (buildable instanceof Structure) {
			if (!buildable.isComplete()) {
				throw new CivException(CivSettings.localize.localizedString("missionBook_sabatoge_errorIncomplete"));
			}
			
			if (buildable.isDestroyed()) {
				throw new CivException(CivSettings.localize.localizedString("var_missionBook_sabatoge_errorDestroyed",buildable.getDisplayName()));
			}
		}

        if (buildable instanceof TradeOutpost || buildable instanceof FishingBoat) {
            throw new CivException(CivSettings.localize.localizedString("var_buildable_cannotSabotaged", "§6" + buildable.getDisplayName() + "§c"));
        }
        
		if (buildable instanceof Wonder) {
			// Create a new mission and with the penalties.
			mission = CivSettings.missions.get("spy_sabotage_wonder");
		}
		
		double failMod = 1.0;
		if (resident.getTown().getBuffManager().hasBuff("buff_sabotage")) {
			failMod = resident.getTown().getBuffManager().getEffectiveDouble("buff_sabotage");
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("missionBook_sabatoge_buffGoodie"));
		}
		
		if (processMissionResult(player, cc.getTown(), mission, failMod, 1.0)) {
			CivMessage.global(CivColor.Yellow+CivSettings.localize.localizedString("missionBook_sabatoge_alert1")+CivColor.White+" "+CivSettings.localize.localizedString("missionBook_sabatoge_alert2",buildable.getDisplayName(),cc.getTown().getName()));
			buildable.setHitpoints(0);
			buildable.fancyDestroyStructureBlocks();
			buildable.save();
			
			if (buildable instanceof Wonder) {
				Wonder wonder = (Wonder)buildable;
				wonder.unbindStructureBlocks();
				try {
					wonder.delete();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	private static void performPirate(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorNotResident"));
		}
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		if (cc == null || cc.getCiv() == resident.getTown().getCiv()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorBorder"));
		}
		
		// Check that the player is within range of the town hall.
		Structure tradeoutpost = cc.getCiv().getNearestStructureInTowns(player.getLocation());
		if (!(tradeoutpost instanceof TradeOutpost)) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_pirate_errorNottrade"));
		}
		if ((tradeoutpost instanceof FishingBoat)) {
			if (tradeoutpost.getTown().getBuffManager().hasBuff("buff_ingermanland_fishing_boat_immunity")){
				throw new CivException(CivSettings.localize.localizedString("missionBook_pirate_errorImmunity"));
			}
		}
		
		double distance = player.getLocation().distance(((TradeOutpost)tradeoutpost).getTradeOutpostTower().getLocation());
		if (distance > mission.range) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_pirate_errorTooFar"));
		}
		
		TradeOutpost outpost = (TradeOutpost)tradeoutpost;
		ItemStack stack = outpost.getItemFrameStore().getItem(); 
		
		if (stack == null || ItemManager.getTypeId(stack) == CivData.AIR) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_pirate_errorNoGoodie"));
		}
		
		if(processMissionResult(player, cc.getTown(), mission)) {
			outpost.getItemFrameStore().clearItem();
			player.getWorld().dropItem(player.getLocation(), stack);
		
			CivMessage.sendSuccess(player, CivSettings.localize.localizedString("missionBook_pirate_success"));
			CivMessage.sendTown(cc.getTown(), CivColor.Rose+CivSettings.localize.localizedString("missionBook_pirate_alert")+" "+outpost.getGood().getInfo().name+" @ "+outpost.getCorner());
		}
	}
	
	private static void performPosionGranary(Player player, ConfigMission mission) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorNotResident"));
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorBorder"));
		}
		
		// Check that the player is within range of the town hall.
		Structure granary = tc.getTown().getNearestStrucutre(player.getLocation());
		if (!(granary instanceof Granary)) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_poison_errorNotGranary"));
		}
		
		double distance = player.getLocation().distance(granary.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_poison_errorTooFar"));
		}
		
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("posiongranary:"+tc.getTown().getName());
		if (entries != null && entries.size() != 0) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_poison_errorPoisoned"));
		}
		
		double failMod = 1.0;
		if (resident.getTown().getBuffManager().hasBuff("buff_espionage")) {
			failMod = resident.getTown().getBuffManager().getEffectiveDouble("buff_espionage");
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("missionBook_poison_goodie"));
		}
		
		if (processMissionResult(player, tc.getTown(), mission, failMod, 1.0)) {
			int min;
			int max;
			try {
				min = CivSettings.getInteger(CivSettings.unitMaterialsConfig, "espionage.poison_granary_min_ticks");
				max = CivSettings.getInteger(CivSettings.unitMaterialsConfig, "espionage.poison_granary_max_ticks");
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalException"));
			}
			
			Random rand = new Random();
			int posion_ticks = rand.nextInt((max -min)) + min;
			String value = ""+posion_ticks;
			
			CivGlobal.getSessionDB().add("posiongranary:"+tc.getTown().getName(), value, tc.getTown().getId(), tc.getTown().getId(), granary.getId());
			
			try {
				double famine_chance = CivSettings.getDouble(CivSettings.unitMaterialsConfig, "espionage.poison_granary_famine_chance");
				
				if (rand.nextInt(100) < (int)(famine_chance*100)) {
					
					for (Structure struct : tc.getTown().getStructures()) {
						if (struct instanceof Cottage) {
							((Cottage)struct).delevel();
						}
					}
					
					CivMessage.global(CivColor.Yellow+CivSettings.localize.localizedString("missionBook_sabatoge_alert1")+CivColor.White+" "+CivSettings.localize.localizedString("var_missionBook_poison_alert1",tc.getTown().getName()));
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalException"));
			}
			
			CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_missionBook_poison_success1",posion_ticks));
		}
	}
	
	private static void performStealTreasury(Player player, ConfigMission mission) throws CivException {
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorNotResident"));
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorBorder"));
		}
		
		// Check that the player is within range of the town hall.
		TownHall townhall = tc.getTown().getTownHall();
		if (townhall == null) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_steal_errorNoTownHall"));
		}
		
		double distance = player.getLocation().distance(townhall.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_steal_errorTooFar"));
		}
		
		double failMod = 1.0;
		if (resident.getTown().getBuffManager().hasBuff("buff_dirty_money")) {
			failMod = resident.getTown().getBuffManager().getEffectiveDouble("buff_dirty_money");
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("missionBook_steal_goodie"));
		}
		
		if(processMissionResult(player, tc.getTown(), mission, failMod, 1.0)) {
			
			double amount = (int)(tc.getTown().getTreasury().getBalance()*0.2);
			if (amount > 0) {
				tc.getTown().getTreasury().withdraw(amount);
				resident.getTown().getTreasury().deposit(amount);
			}
			
			CivMessage.sendSuccess(player, CivSettings.localize.localizedString("missionBook_steal_success")+" "+amount+" "+CivSettings.CURRENCY_NAME+" -> "+tc.getTown().getName());
		}
	}
	
	private static void performInvestigateTown(Player player, ConfigMission mission) throws CivException {
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorNotResident"));
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorBorder"));
		}
		
		if(processMissionResult(player, tc.getTown(), mission)) {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta meta = (BookMeta) book.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(CivSettings.localize.localizedString("missionBook_investigate_addLore"));
			
			meta.setAuthor(CivSettings.localize.localizedString("missionBook_investigate_setAuthor"));
			meta.setTitle(CivSettings.localize.localizedString("missionBook_investigate_setTitle"));
			
		//	ArrayList<String> out = new ArrayList<String>();
			String out = "";
			
			out += ChatColor.UNDERLINE+CivSettings.localize.localizedString("Town")+tc.getTown().getName()+"\n"+ChatColor.RESET;
			out += ChatColor.UNDERLINE+CivSettings.localize.localizedString("Civilization")+tc.getTown().getCiv().getName()+"\n\n"+ChatColor.RESET;
			
			SimpleDateFormat sdf = CivGlobal.dateFormat;
			out += CivSettings.localize.localizedString("Time")+" "+sdf.format(new Date())+"\n";
			out += (CivSettings.localize.localizedString("Treasury")+" "+tc.getTown().getTreasury().getBalance()+"\n");
			out += (CivSettings.localize.localizedString("Hammers")+" "+tc.getTown().getHammers().total+"\n");
			out += (CivSettings.localize.localizedString("Culture")+" "+tc.getTown().getCulture().total+"\n");
			out += (CivSettings.localize.localizedString("cmd_town_growth")+" "+tc.getTown().getGrowth().total+"\n");
			out += (CivSettings.localize.localizedString("BeakersCiv")+" "+tc.getTown().getBeakers().total+"\n");
			if (tc.getTown().getCiv().getResearchTech() != null) {
				out += (CivSettings.localize.localizedString("Researching")+" "+tc.getTown().getCiv().getResearchTech().name+"\n");
			} else {
				out += (CivSettings.localize.localizedString("ResearchingNothing")+"\n");
			}
			
			BookUtil.paginate(meta, out);
			
			out = ChatColor.UNDERLINE+CivSettings.localize.localizedString("cmd_civ_info_upkeepHeading")+"\n\n"+ChatColor.RESET;
			try {
				out += CivSettings.localize.localizedString("cmd_town_info_spreadUpkeep")+" "+tc.getTown().getSpreadUpkeep()+"\n";
				out += CivSettings.localize.localizedString("cmd_town_info_structuresUpkeep")+" "+tc.getTown().getStructureUpkeep()+"\n";
				out += CivSettings.localize.localizedString("Total")+" "+tc.getTown().getTotalUpkeep();
				BookUtil.paginate(meta, out);
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalException"));
			}
			
			
			meta.setLore(lore);
			book.setItemMeta(meta);
			
			HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(book);
			for (ItemStack stack : leftovers.values()) {
				player.getWorld().dropItem(player.getLocation(), stack);
			}
			
			player.updateInventory();
			
			CivMessage.sendSuccess(player, CivSettings.localize.localizedString("missionBook_investigate_success"));
		}
	}
	
	private static void performSubertGov(Player player, ConfigMission mission) throws CivException {		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorNotResident"));
		}
		
		// Must be within enemy town borders.
		ChunkCoord coord = new ChunkCoord(player.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(coord);
		
		if (tc == null || tc.getTown().getCiv() == resident.getTown().getCiv()) {
			throw new CivException(CivSettings.localize.localizedString("missionBook_errorBorder"));
		}
		
		Town town = tc.getTown();
		Civilization civ = town.getCiv();
		
		if (town.getBuffManager().hasBuff("buff_notre_dame_no_anarchy")) {
			throw new CivException(CivSettings.localize.localizedString("var_missionBook_subvert_errorNotreDame",civ.getName()));
		}
				
		// Check that the player is within range of the town hall.
		Structure capitol = town.getNearestStrucutre(player.getLocation());
		if (!(capitol instanceof Capitol)) {
			throw new CivException(CivSettings.localize.localizedString("var_missionBook_subvert_errorNotCapitol",capitol.getDisplayName(),civ.getName()));
		}
		
		double distance = player.getLocation().distance(capitol.getCorner().getLocation());
		if (distance > mission.range) {
			throw new CivException(CivSettings.localize.localizedString("var_missionBook_subvert_errorTooFar", mission.range));
		}
		
		if (civ.getGovernment().id == "gov_anarchy") {
			throw new CivException(CivSettings.localize.localizedString("var_missionBook_subvert_errorInAnarchy",civ.getName()));
		} else if (civ.getGovernment().id == "gov_tribalism") {
			throw new CivException(CivSettings.localize.localizedString("var_missionBook_subvert_errorInTribalism",civ.getName()));
		}
		
		if (processMissionResult(player, tc.getTown(), mission)) {
			civ.changeGovernment(civ, civ.getGovernment(), true);
			CivMessage.global(CivColor.Yellow+CivSettings.localize.localizedString("missionBook_sabatoge_alert1")+CivColor.White+" "+CivSettings.localize.localizedString("var_missionBook_subvert_alert1",civ.getName()));
			
			CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_missionBook_subvert_success1",civ.getName()));
		}
		
	}
	
	private static void performInciteRiots(Player player, ConfigMission mission) throws CivException {
		throw new CivException(CivSettings.localize.localizedString("missionBook_Invalid"));
	}

}
