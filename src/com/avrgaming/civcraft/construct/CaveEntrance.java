package com.avrgaming.civcraft.construct;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.war.War;

public class CaveEntrance extends Construct {

	private Cave cave;

	public CaveEntrance(Cave cave) {
		this.cave = cave;
	}

	@Override
	public void commandBlockRelatives(BlockCoord absCoord, SimpleBlock sb) {
		ConstructSign structSign;
		switch (sb.command) {
		case "/entrance":
			structSign = CivGlobal.getConstructSign(absCoord);
			if (structSign == null)
				structSign = new ConstructSign(absCoord, this);
			ItemManager.setTypeIdAndData(absCoord.getBlock(), sb.getType(), sb.getData(), true);

			structSign.setDirection(ItemManager.getData(absCoord.getBlock().getState()));
//			for (String key : sb.keyvalues.keySet()) {
//				structSign.setType(key);
//				structSign.setAction(sb.keyvalues.get(key));
//			}
			structSign.setOwner(this);
			structSign.setText(new String[] { "", "Нажми", "что бы", "спустится" });
			structSign.setAction("entrance");
			structSign.update();
			this.addBuildableSign(structSign);
			CivGlobal.addConstructSign(structSign);
			break;
		}

	}

	@Override
	public void processSignAction(Player player, ConstructSign sign, PlayerInteractEvent event) {
		// int special_id = Integer.valueOf(sign.getAction());
		Resident resident = CivGlobal.getResident(player);
		if (resident == null)
			return;
		if (War.isWarTime())
			return;

//		Boolean hasPermission = false;
//		if ((resident.getTown().isMayor(resident)) || (resident.getTown().getAssistantGroup().hasMember(resident))
//				|| (resident.getCiv().getLeaderGroup().hasMember(resident))
//				|| (resident.getCiv().getAdviserGroup().hasMember(resident))) {
//			hasPermission = true;
//		}
		switch (sign.getAction()) {
		case "entrance":
			if (resident.getConstructSignConfirm() != null && resident.getConstructSignConfirm().equals(sign)) {
			CivMessage.send(player,
					CivColor.LightGreen + CivSettings.localize.localizedString("capitol_respawningAlert"));
			player.teleport(cave.getSpawns().get("1").getLocation());
			this.cave.activateMobSpawners();
			} else {
				this.showInfo(player);
				resident.setConstructSignConfirm(sign);
			}
			break;
		}
	}
	
	private void showInfo(Player player) {
		CivMessage.send(player, "Это вход в пещеру под названием " + this.getDisplayName());
		CivMessage.send(player, "Здесь писать информацию о пещере");
		CivMessage.send(player, "Здесь писать информацию о пещере");
		CivMessage.send(player, "Здесь писать информацию о пещере");
		CivMessage.send(player, "Здесь писать информацию о пещере");
		CivMessage.send(player, "Здесь писать информацию о пещере");
		CivMessage.send(player, "Для входа впещеру нажмите на табличку ещё раз");
	}

	@Override
	public void load(ResultSet rs) {
	}

	@Override
	public void saveNow() throws SQLException {
	}

	@Override
	public String getDisplayName() {
		return cave.getDisplayName();
	}

	@Override
	public void processUndo() throws CivException {
	}

	@Override
	public void build(Player player) throws Exception {
		this.getTemplate().buildTemplate(getCorner());
		this.bindBlocks();
	}

	@Override
	public String getDynmapDescription() {
		return null;
	}

	@Override
	public String getMarkerIconName() {
		return null;
	}

	@Override
	public void onLoad() throws CivException {
	}

	@Override
	public void onUnload() {
	}

	@Override
	public void onDamage(int amount, World world, Player player, BlockCoord coord, ConstructDamageBlock hit) {
		// TODO Автоматически созданная заглушка метода

	}

	@Override
	public void onDamageNotification(Player player, ConstructDamageBlock hit) {
		// TODO Автоматически созданная заглушка метода

	}

}
