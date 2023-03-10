/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.command;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuildableInfo;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.loregui.GuiPage;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.threading.tasks.BuildAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

import gpl.FancyMessage;

public class BuildCommand extends CommandBase {
	public FancyMessage buildMessage;


	@Override
	public void init() {
		command = "/build";
		displayName = CivSettings.localize.localizedString("cmd_build_Desc");
		cs.sendUnknownToDefault = true;
		
		cs.add("list", CivSettings.localize.localizedString("cmd_build_listDesc"));
		cs.add("progress", CivSettings.localize.localizedString("cmd_build_progressDesc"));
		cs.add("repairnearest", CivSettings.localize.localizedString("cmd_build_repairnearestDesc"));
		cs.add("undo", CivSettings.localize.localizedString("cmd_build_undoDesc"));
		cs.add("demolish", CivSettings.localize.localizedString("cmd_build_demolishDesc"));
		cs.add("demolishnearest", CivSettings.localize.localizedString("cmd_build_demolishnearestDesc"));
		cs.add("refreshnearest", CivSettings.localize.localizedString("cmd_build_refreshnearestDesc"));
		cs.add("validatenearest", CivSettings.localize.localizedString("cmd_build_validateNearestDesc"));
        cs.add("calc", CivSettings.localize.localizedString("cmd_build_calc_Desc"));
        String info = CivSettings.localize.localizedString("cmd_build_help1") + " " + CivSettings.localize.localizedString("cmd_build_help2");
        info = info.replace("[", "??a<");
        info = info.replace("]", ">??f");
        info = info.replace("(", "??2(");
        info = info.replace(")", ")??f");
        CivMessage.send((Object)this.sender, (String)("??e" + this.command + "??f" + ": " + info));
        this.buildMessage = (new FancyMessage("??e" + this.command)).suggest(this.command).tooltip(info).then("??f: " + info);
	}
	public void calc_cmd() throws CivException {
	      Town town = this.getSelectedTown();
	      if (town.build_tasks.size() != 0 && !town.build_tasks.isEmpty()) {
	         CivGlobal.dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
	         town.build_tasks.stream().map((task) -> {
	            return task.buildable;
	         }).forEachOrdered((b) -> {
	            double mins = (b.getHammerCost() - b.getBuiltHammers()) / 2.0D / town.getHammers().total * 60.0D;
	            long timeNow = Calendar.getInstance().getTimeInMillis();
	            double seconds = mins * 60.0D;
	            long end = (long)((double)timeNow + 1000.0D * seconds);
	            String messageSender = "??a" + CivSettings.localize.localizedString("cmd_build_calc_result", "??2" + b.getDisplayName() + "??a", "??c" + CivGlobal.dateFormat.format(end) + "??a");
	            String messageTown = CivSettings.localize.localizedString("cmd_build_calc_result", "??2" + b.getDisplayName() + CivColor.RESET, "??c" + CivGlobal.dateFormat.format(end) + CivColor.RESET);
	            CivMessage.send((Object)this.sender, (String)messageSender);
	            CivMessage.sendTown(town, messageTown);
	         });
	      } else {
	         throw new CivException(CivSettings.localize.localizedString("cmd_build_notBuilding"));
	      }
	   }
	
	public void validatenearest_cmd() throws CivException {
		Player player = getPlayer();
		Resident resident = getResident();
		Buildable buildable = CivGlobal.getNearestBuildable(player.getLocation());
		
		if (buildable.getTown() != resident.getTown()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_validateNearestYourTownOnly"));
		}
		
		if (War.isWarTime()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_validatenearestNotDuringWar"));
		}
		
		if (buildable.isIgnoreFloating()) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_build_validateNearestExempt",buildable.getDisplayName()));
		}
		
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_cmd_build_validateNearestSuccess",buildable.getDisplayName(),buildable.getCenterLocation().toVector()));
		buildable.validate(player);
	}
	
	public void refreshnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Resident resident = getResident();
		town.refreshNearestBuildable(resident);
	}
	
	public void repairnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		if (War.isWarTime()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_repairNotDuringWar"));
		}
		
		Structure nearest = town.getNearestStrucutre(player.getLocation());
			
		if (nearest == null) {
			throw new CivException (CivSettings.localize.localizedString("cmd_build_Invalid"));
		}
		
		if (!nearest.isDestroyed()) {
			throw new CivException (CivSettings.localize.localizedString("var_cmd_build_repairNotDestroyed",nearest.getDisplayName(),nearest.getCorner()));
		}
		
		if (!town.getCiv().hasTechnology(nearest.getRequiredTechnology())) {
			throw new CivException (CivSettings.localize.localizedString("var_cmd_build_repairMissingTech",nearest.getDisplayName(),nearest.getCorner()));
		}
	
		if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("var_cmd_build_repairConfirmPrompt",
					CivColor.Yellow+nearest.getDisplayName()+CivColor.LightGreen,CivColor.Yellow+nearest.getCorner()+CivColor.LightGreen,CivColor.Yellow+nearest.getRepairCost()+CivColor.LightGreen,CivColor.Yellow+CivSettings.CURRENCY_NAME+CivColor.LightGreen));
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("cmd_build_repairConfirmPrompt2"));
			return;
		}
		
		town.repairStructure(nearest);		
		CivMessage.sendSuccess(player, nearest.getDisplayName()+" "+CivSettings.localize.localizedString("Repaired"));
	}
	
	public void demolishnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		Structure nearest = town.getNearestStrucutre(player.getLocation());
		
		if (nearest == null) {
			throw new CivException (CivSettings.localize.localizedString("cmd_build_Invalid"));
		}
		
		if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("var_cmd_build_demolishNearestConfirmPrompt",CivColor.Yellow+nearest.getDisplayName()+CivColor.LightGreen,
					CivColor.Yellow+nearest.getCorner()+CivColor.LightGreen));
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("cmd_build_demolishNearestConfirmPrompt2"));
						
			nearest.flashStructureBlocks();
			return;
		}
		
		town.demolish(nearest, false);
		CivMessage.sendSuccess(player, nearest.getDisplayName()+" at "+nearest.getCorner()+" "+CivSettings.localize.localizedString("adcmd_build_demolishComplete"));
	}
	
	
	public void demolish_cmd() throws CivException {
		Town town = getSelectedTown();
		
		
		if (args.length < 2) {
			CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_demolishHeader"));
			for (Structure struct : town.getStructures()) {
				CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_build_demolish",struct.getDisplayName(),CivColor.Yellow+struct.getCorner().toString()+CivColor.White));
			}
			return;
		}
		
		try {
			BlockCoord coord = new BlockCoord(args[1]);
			Structure struct = town.getStructure(coord);
			if (struct == null) {
				CivMessage.send(sender, CivColor.Rose+" "+CivSettings.localize.localizedString("NoStructureAt")+" "+args[1]);
				return;
			}
			struct.getTown().demolish(struct, false);
			CivMessage.sendTown(struct.getTown(), struct.getDisplayName()+" "+CivSettings.localize.localizedString("adcmd_build_demolishComplete"));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_build_demolishFormatError"));
		}
	}
	
	public void undo_cmd() throws CivException {
		Town town = getSelectedTown();
		town.processUndo();
	}
	
	public void progress_cmd() throws CivException {
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_undoHeader"));
		Town town = getSelectedTown();
		for (BuildAsyncTask task : town.build_tasks) {
			Buildable b = task.buildable;
			DecimalFormat df = new DecimalFormat();
			double total = b.getHammerCost();
			double current = b.getBuiltHammers();
			double builtPercentage = current/total;
			builtPercentage = Math.round(builtPercentage *100);
			
			CivMessage.send(sender, CivColor.LightPurple+b.getDisplayName()+": "+CivColor.Yellow+builtPercentage+"% ("+df.format(current) + "/"+total+")"+
					CivColor.LightPurple+" Blocks "+CivColor.Yellow+"("+b.builtBlockCount+"/"+b.getTotalBlockCount()+")");
			
			//CivMessage.send(sender, CivColor.LightPurple+b.getDisplayName()+" "+CivColor.Yellow+"("+
				//	b.builtBlockCount+" / "+b.getTotalBlockCount()+")");
		}
		
	}

	public void list_available_structures() throws CivException {
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_listHeader"));
		Town town = getSelectedTown();
		for (ConfigBuildableInfo sinfo : CivSettings.structures.values()) {
			if (sinfo.isAvailable(town)) {
				String leftString = "";
				if (sinfo.limit == 0) {
					leftString = CivSettings.localize.localizedString("Unlimited");
				} else {
					leftString = ""+(sinfo.limit - town.getStructureTypeCount(sinfo.id));
				}
				
				CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+" "+
						CivColor.Yellow+
						CivSettings.localize.localizedString("Cost")+" "+sinfo.cost+" "+
						CivSettings.localize.localizedString("Upkeep")+" "+sinfo.upkeep+" "+CivSettings.localize.localizedString("Hammers")+" "+sinfo.hammer_cost+" "+ 
						CivSettings.localize.localizedString("Remaining")+" "+leftString);
			}
		}
	}
	
	public void list_available_wonders() throws CivException {
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_listWondersHeader"));
		Town town = getSelectedTown();
		for (ConfigBuildableInfo sinfo : CivSettings.wonders.values()) {
			if (sinfo.isAvailable(town)) {
				String leftString = "";
				if (sinfo.limit == 0) {
					leftString = CivSettings.localize.localizedString("Unlimited");
				} else {
					leftString = ""+(sinfo.limit - town.getStructureTypeCount(sinfo.id));
				}
				
				if (Wonder.isWonderAvailable(sinfo.id)) {
					double rate = 1.0;
	                rate -= town.getBuffManager().getEffectiveDouble("buff_rush");
	                rate -= town.getBuffManager().getEffectiveDouble("buff_grandcanyon_rush");
	                rate -= town.getBuffManager().getEffectiveDouble("buff_mother_tree_tile_improvement_cost");
					CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+" "+
							CivColor.Yellow+
							CivSettings.localize.localizedString("Cost")+" "+sinfo.cost+" "+
							CivSettings.localize.localizedString("Upkeep")+" "+sinfo.upkeep+" "+CivSettings.localize.localizedString("Hammers")+" "+sinfo.hammer_cost * rate +" "+
							CivSettings.localize.localizedString("Remaining")+" "+leftString);
				} else {
					Wonder wonder = CivGlobal.getWonderByConfigId(sinfo.id);
					CivMessage.send(sender, CivColor.LightGray+sinfo.displayName+" Cost: "+sinfo.cost+" - "+CivSettings.localize.localizedString("var_cmd_build_listWonderAlreadyBuild",wonder.getTown().getName(),wonder.getTown().getCiv().getName()));
				}
			}
		}
	}
	
	public void list_cmd() throws CivException {
		this.list_available_structures();
		this.list_available_wonders();
	}
	
	@Override
	public void doDefaultAction() throws CivException {
        if (this.args.length == 0) {
            this.showHelp();
            final Resident resident = this.getResident();
            GuiPage.showStructPage(resident);
            return;
        }
		String fullArgs = "";
		for (String arg : args) {
			fullArgs += arg + " ";
		}
		fullArgs = fullArgs.trim();
		
		buildByName(fullArgs);
	}

	public void preview_cmd() throws CivException {
		String fullArgs = this.combineArgs(this.stripArgs(args, 1));
		
		ConfigBuildableInfo sinfo = CivSettings.getBuildableInfoByName(fullArgs);
		if (sinfo == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_defaultUnknownStruct")+" "+fullArgs);
		}
		
		Town town = getSelectedTown();
		if (sinfo.isWonder) {
			Wonder wonder = Wonder.newWonder(getPlayer().getLocation(), sinfo.id, town);
			try {
				wonder.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		} else {
		Structure struct = Structure.newStructure(getPlayer().getLocation(), sinfo.id, town);
			try {
				struct.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		}
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_build_showPreviewSuccess"));
	}
	
	
	private void buildByName(String fullArgs) throws CivException {
		ConfigBuildableInfo sinfo = CivSettings.getBuildableInfoByName(fullArgs);

		if (sinfo == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_defaultUnknownStruct")+" "+fullArgs);
		}
		
		Town town = getSelectedTown();
		if (sinfo.id.equals("wonder_stock_exchange") && !town.canBuildStock(this.getPlayer())) {
            throw new CivException(CivColor.Red + CivSettings.localize.localizedString("var_buildStockExchange_nogoodCondition", "http://wiki.minetexas.com/index.php/Stock_Exchange"));
        }
		if (sinfo.isWonder) {
			Wonder wonder = Wonder.newWonder(getPlayer().getLocation(), sinfo.id, town);
			try {
				wonder.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		} else {
			Structure struct = Structure.newStructure(getPlayer().getLocation(), sinfo.id, town);
			try {
				struct.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		}
		
	}

	@Override
	public void showHelp() {
		showBasicHelp();		
		CivMessage.send(sender, CivColor.LightPurple+command+" "+CivColor.Yellow+CivSettings.localize.localizedString("cmd_build_help1")+" "+
				CivColor.LightGray+CivSettings.localize.localizedString("cmd_build_help2"));
	}

	@Override
	public void permissionCheck() throws CivException {
		validMayorAssistantLeader();
	}

}
