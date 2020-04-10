package com.avrgaming.civcraft.construct;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCave;
import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.mythicmob.MobSpawner;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.war.War;

import javafx.geometry.Rectangle2D;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Cave extends Construct {

	public static String worldCavesName = "caves";
	public static Integer multiplerCoord = 32;

	private CaveEntrance caveEntrance;
	private ConfigCave caveConfig;
	private HashMap<String, BlockCoord> spawns = new HashMap<String, BlockCoord>();
	private HashMap<BlockCoord, MobSpawner> mobspawners = new HashMap<BlockCoord, MobSpawner>();
	private Set<Resident> residents = new HashSet<Resident>();

	public Cave(String id, BlockCoord cornerEntrance) throws CivException {
		this.caveEntrance = new CaveEntrance(this);
		this.caveConfig = CivSettings.caves.get(id);
		if (caveConfig == null)
			throw new CivException("Не найден CaveConfigId" + id);
		try {
			this.setName(caveConfig.name);
		} catch (InvalidNameException e) {
			e.printStackTrace();
		}
		this.setTemplate(Template.getTemplate(Template.getCaveFilePath(caveConfig.template_name)));
		this.caveEntrance.setTemplate(Template.getTemplate(Template.getCaveFilePath(caveConfig.template_entrance)));
		this.setCornerEntrance(cornerEntrance);
		this.hitpoints = 0;

//		this.loadSettings();
	}

	public static void newCaveEntrance(String id, BlockCoord cornerEntrance) throws Exception {
		Cave cave = new Cave(id, cornerEntrance);
		cave.build(null);
		CivGlobal.addCave(cave);
		cave.save();
	}

	public Cave(ResultSet rs) throws SQLException, InvalidNameException {
		this.caveEntrance = new CaveEntrance(this);
		this.load(rs);
	}

	public static final String TABLE_NAME = "CAVE_ENTRANCE";

	public static void init() throws SQLException {
		if (!SQL.hasTable(TABLE_NAME)) {
			String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME + " (" + //
					"`id` int(11) unsigned NOT NULL auto_increment," + //
					"`name` VARCHAR(64) NOT NULL," + //
					"`config_id` mediumtext DEFAULT NULL," + //
					"`owner_civ_id` int(11)," + //
					"`coord` mediumtext DEFAULT NULL," + //
					"`corner_entrance` mediumtext DEFAULT NULL," + //
					"`template_name` mediumtext DEFAULT NULL, " + //
					"`template_entrance` mediumtext DEFAULT NULL, " + //
					"PRIMARY KEY (`id`)" + ")";
			SQL.makeTable(table_create);
			CivLog.info("Created " + TABLE_NAME + " table");
		} else {
			CivLog.info(TABLE_NAME + " table OK!");
		}
	}

	@Override
	public void load(ResultSet rs) throws SQLException, InvalidNameException {
		this.setId(rs.getInt("id"));
		this.setName(rs.getString("name"));
		this.setCaveConfig(CivSettings.caves.get(rs.getString("config_id")));
		this.setSQLOwner(CivGlobal.getCivFromId(rs.getInt("owner_civ_id")));
		this.setCorner(new BlockCoord(rs.getString("coord")));
		this.setCornerEntrance(new BlockCoord(rs.getString("corner_entrance")));
		this.setTemplate(Template.getTemplate(rs.getString("template_name")));
		this.caveEntrance.setTemplate(Template.getTemplate(rs.getString("template_entrance")));

		this.bindBlocks();
	}

	@Override
	public void saveNow() throws SQLException {
		HashMap<String, Object> hashmap = new HashMap<String, Object>();

		hashmap.put("name", this.getName());
		hashmap.put("config_id", this.getCaveConfig().id);
		hashmap.put("owner_civ_id", (this.getCiv() != null) ? this.getCiv().getId() : 0);
		hashmap.put("coord", this.getCorner().toString());
		hashmap.put("corner_entrance", this.getCornerEntrance().toString());
		hashmap.put("template_name", this.getTemplate().getFilepath());
		hashmap.put("template_entrance", this.caveEntrance.getTemplate().getFilepath());

		SQL.updateNamedObject(this, hashmap, TABLE_NAME);
	}

	public void bindBlocks() {
		super.bindBlocks();
		caveEntrance.bindBlocks();
	}

	public BlockCoord getTeleportCoord() {
		return spawns.get("1");
	}

	@Override
	public void delete() throws SQLException {
	}

	@Override
	public String getDisplayName() {
		return this.getName();
	}

	@Override
	public void processUndo() throws CivException {
	}

	@Override
	public void build(Player player) throws Exception {
		BlockCoord corner = new BlockCoord();
		corner.setWorldname(Cave.worldCavesName);
		corner.setX(caveEntrance.getCorner().getX() * Cave.multiplerCoord);
		corner.setY(10);
		corner.setZ(caveEntrance.getCorner().getZ() * Cave.multiplerCoord);
		this.corner = corner;
		this.setCenterLocation(this.getCorner().getLocation().add(this.getTemplate().size_x / 2,
				this.getTemplate().size_y / 2, this.getTemplate().size_z / 2));

		this.checkBlockPermissionsAndRestrictions(null);

		this.getTemplate().buildTemplate(corner);
		this.bindBlocks();

		caveEntrance.build(player);

		try {
			this.saveNow();
		} catch (SQLException var7) {
			var7.printStackTrace();
			throw new CivException("Internal SQL Error.");
		}
	}

	@Override
	public void checkBlockPermissionsAndRestrictions(Player player) throws CivException {
		for (Cave cave : CivGlobal.getCaves()) {
			Rectangle2D caveR = new Rectangle2D(cave.getCorner().getX(), cave.getCorner().getZ(),
					cave.getCorner().getX() + cave.getTemplate().getSize_x(),
					cave.getCorner().getZ() + cave.getTemplate().getSize_z());
			Rectangle2D thisR = new Rectangle2D(this.getCorner().getX(), this.getCorner().getZ(),
					this.getCorner().getX() + this.getTemplate().getSize_x(),
					this.getCorner().getZ() + this.getTemplate().getSize_z());
			if (caveR.intersects(thisR))
				throw new CivException("Есть пересечение с другими пещерами");
		}
	}

	@Override
	public void commandBlockRelatives(BlockCoord absCoord, SimpleBlock sb) {
		ConstructSign structSign;
		switch (sb.command) {
		case "/spawn":
			spawns.put(sb.keyvalues.get("id"), absCoord);
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
			structSign.setText(new String[] { "", "Нажми", "что бы", "переместится" });
			structSign.setAction("spawn");
			structSign.update();
			this.addBuildableSign(structSign);
			CivGlobal.addConstructSign(structSign);
			break;
		case "/mobspawn":
			MobSpawner ms = new MobSpawner(absCoord.getLocation(), this.caveConfig.mobId);
			mobspawners.put(absCoord, ms);
			ItemManager.setTypeIdAndData(absCoord.getBlock(), 397, 2, true);
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
		case "spawn":
			if (resident.getConstructSignConfirm() != null && resident.getConstructSignConfirm().equals(sign)) {
				CivMessage.send(player,
						CivColor.LightGreen + CivSettings.localize.localizedString("capitol_respawningAlert"));
				Resident res = CivGlobal.getResident(player);
				Location loc;
				if (res.getTown() != null)
					loc = res.getTown().getTownHall().getRandomRespawnPoint().getLocation();
				else
					loc = Bukkit.getWorld("world").getSpawnLocation();
				player.teleport(loc);
				break;
			} else {
				this.showInfo(player);
				resident.setConstructSignConfirm(sign);
			}
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
	
	public void activateMobSpawners() {
		for (MobSpawner ms : mobspawners.values()) {
			ms.activate();
		}
	}

	private long endTime = 0;

	@Override
	public void onSecondUpdate() {
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
	}

	@Override
	public void onDamageNotification(Player player, ConstructDamageBlock hit) {
	}

	public BlockCoord getCornerEntrance() {
		return caveEntrance.getCorner();
	}

	public void setCornerEntrance(BlockCoord bc) {
		caveEntrance.setCorner(bc);
	}
}
