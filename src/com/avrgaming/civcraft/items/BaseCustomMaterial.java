package com.avrgaming.civcraft.items;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigMaterial;
import com.avrgaming.civcraft.items.components.ItemComponent;
import com.avrgaming.civcraft.lorestorage.ItemChangeResult;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.BuildableDamageBlock;

import gpl.AttributeUtil;

public abstract class BaseCustomMaterial extends CustomMaterial {

	protected ConfigMaterial configMaterial;

	/* Components that are registered to this object. */
	public HashMap<String, ItemComponent> components = new HashMap<String, ItemComponent>();

	public BaseCustomMaterial(String id, int typeID, short damage) {
		super(id, typeID, damage);
	}

	public static BaseCustomMaterial getBaseCustomMaterial(ItemStack stack) {
		if (stack == null) return null;
		CustomMaterial mat = getCustomMaterial(getMID(stack));
		if (mat instanceof BaseCustomMaterial) return (BaseCustomMaterial) mat;
		return null;
	}

	public static BaseCustomMaterial getBaseCustomMaterial(String mid) {
		CustomMaterial mat = getCustomMaterial(mid.toLowerCase());
		if (mat instanceof BaseCustomMaterial) return (BaseCustomMaterial) mat;
		return null;
	}

//	public static Collection<BaseCustomMaterial> getAllBaseCustomMaterial() {
//		return getAllLoreMaterial();
//	}

	public static boolean isBaseCustomMaterial(ItemStack stack) {
		BaseCustomMaterial material = (BaseCustomMaterial) getCustomMaterial(stack);
		return material != null;
	}

	public static void buildStaticMaterials() {
		/* Loads in materials from configuration file. */
		for (ConfigMaterial cfgMat : CivSettings.craftableMaterials.values()) {
			CraftableCustomMaterial loreMat = new CraftableCustomMaterial(cfgMat.id, cfgMat.item_id, (short) cfgMat.item_data);
			loreMat.setName(cfgMat.name);
			loreMat.setLore(cfgMat.lore);
			loreMat.setCraftable(cfgMat.craftable);
			loreMat.setShaped(cfgMat.shaped);
			loreMat.configMaterial = cfgMat;
			loreMat.buildComponents();
//			materials.put(cfgMat.id, loreMat);
		}
		for (ConfigMaterial cfgMat : CivSettings.unitMaterials.values()) {
			UnitCustomMaterial loreMat = new UnitCustomMaterial(cfgMat.id, cfgMat.item_id, (short) cfgMat.item_data);
			loreMat.setName(cfgMat.name);
			loreMat.setLore(cfgMat.lore);
			loreMat.configMaterial = cfgMat;
			loreMat.buildComponents();
//			materials.put(cfgMat.id, loreMat);
		}
	}

	public void buildComponents() {
		List<HashMap<String, String>> compInfoList = this.configMaterial.components;
		if (compInfoList != null) {
			for (HashMap<String, String> compInfo : compInfoList) {
				String className = "com.avrgaming.civcraft.items.components." + compInfo.get("name");
				Class<?> someClass;

				try {
					someClass = Class.forName(className);
					ItemComponent itemCompClass;
					itemCompClass = (ItemComponent) someClass.newInstance();
					itemCompClass.setName(compInfo.get("name"));

					for (String key : compInfo.keySet()) {
						itemCompClass.setAttribute(key, compInfo.get(key));
					}

					itemCompClass.createComponent();
					this.components.put(itemCompClass.getName(), itemCompClass);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onHit(EntityDamageByEntityEvent event) {
	}

	@Override
	public void onInteract(PlayerInteractEvent event) {
		for (ItemComponent ic : this.components.values()) {
			ic.onInteract(event);
		}
	}

	@Override
	public void onInteractEntity(PlayerInteractEntityEvent event) {
	}

	@Override
	public void onBlockPlaced(BlockPlaceEvent event) {
		for (ItemComponent ic : this.components.values()) {
			if (ic.onBlockPlaced(event)) return;
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
	}

	@Override
	public void onBlockInteract(PlayerInteractEvent event) {
		//event.setCancelled(true);
	}

	@Override
	public void onHold(PlayerItemHeldEvent event) {
		for (ItemComponent comp : this.components.values()) {
			comp.onHold(event);
		}
	}

	@Override
	public void onItemDrop(PlayerDropItemEvent event) {
	}

	@Override
	public void onItemCraft(CraftItemEvent event) {
	}

	@Override
	public void onItemPickup(EntityPickupItemEvent event) {
	}

	@Override
	public void onItemSpawn(ItemSpawnEvent event) {
		for (ItemComponent comp : this.components.values()) {
			comp.onItemSpawn(event);
		}
	}

	@Override
	public boolean onAttack(EntityDamageByEntityEvent event, ItemStack stack) {
		for (ItemComponent comp : this.components.values()) {
			comp.onAttack(event, stack);
		}
		return false;
	}

	@Override
	public void onInvItemDrop(InventoryClickEvent event, Inventory toInv, ItemStack stack) {
		if (event.isCancelled()) return;
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if (!this.isCanUseInventoryTypes(toInv)) {
				CivMessage.sendError(player, "???????????? ???????????????????????? ???????? ?????????????? ?? ?????????????????? " + toInv.getType());
				event.setCancelled(true);
				event.setResult(Result.DENY);
				player.updateInventory();
				return;
			}
		}

		AttributeUtil attrs = new AttributeUtil(stack);
		if (attrs.hasEnhancement("LoreEnhancementUnitItem") && toInv.getType() != InventoryType.PLAYER)
		{
//			CivMessage.sendError(player, "???????????? ???????????????????????? ???????? ?????????????? ?? ?????????????????? " + toInv.getType());
			event.setCancelled(true);
			event.setResult(Result.DENY);
			((Player) event.getWhoClicked()).updateInventory();
			return;
		}
	}

	@Override
	public void onInvItemPickup(InventoryClickEvent event, Inventory fromInv, ItemStack stack) {
	}

	@Override
	public void onInvDrag(InventoryDragEvent event, Inventory toInv, ItemStack stack) {
	}
	
	@Override
	public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
	}

	@Override
	public int onStructureBlockBreak(BuildableDamageBlock sb, int damage) {
		for (ItemComponent component : this.components.values()) {
			damage = component.onStructureBlockBreak(sb, damage);
		}
		return damage;
	}

	@Override
	public void applyAttributes(AttributeUtil attrUtil) {
		for (ItemComponent comp : this.components.values()) {
			comp.onPrepareCreate(attrUtil);
		}
		return;
	}

	public ConfigMaterial getConfigMaterial() {
		return this.configMaterial;
	}

	public String getConfigId() {
		return this.configMaterial.id;
	}

	@Override
	public int hashCode() {
		return this.configMaterial.id.hashCode();
	}

	public Collection<ItemComponent> getComponents() {
		return this.components.values();
	}

	public void addComponent(ItemComponent itemComp) {
		this.components.put(itemComp.getName(), itemComp);
	}

	@Override
	public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
		/* Search components for defense value. */
		for (ItemComponent comp : this.components.values()) {
			comp.onDefense(event, stack);
		}
	}

	public void onItemDurabilityChange(PlayerItemDamageEvent event) {
		for (ItemComponent comp : this.components.values()) {
			comp.onDurabilityChange(event);
		}
	}

	public boolean hasComponent(String string) {
		return this.components.containsKey(string);
	}

	public ItemComponent getComponent(String string) {
		return this.components.get(string);
	}

	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		/* Search components for defense value. */
		for (ItemComponent comp : this.components.values()) {
			comp.onPlayerInteractEntity(event);
		}
	}

	public void onPlayerLeashEvent(PlayerLeashEntityEvent event) {
		for (ItemComponent comp : this.components.values()) {
			comp.onPlayerLeashEvent(event);
		}
	}

	public void onRangedAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		for (ItemComponent comp : this.components.values()) {
			comp.onRangedAttack(event, inHand);
		}
	}

	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemStack stack) {
		ItemChangeResult result = null;
		for (ItemComponent comp : this.components.values()) {
			result = comp.onDurabilityDeath(event, result, stack);
		}
		return result;
	}

	@Override
	public void onInventoryOpen(InventoryOpenEvent event, ItemStack stack) {
		for (ItemComponent comp : this.components.values()) {
			comp.onInventoryOpen(event, stack);
		}
	}

	public boolean isVanilla() {
		return this.configMaterial.vanilla;
	}

	public void rebuildLore() {
	}

	@Override
	public boolean isCanUseInventoryTypes(Inventory inv) {
		switch (inv.getType()) {
			case CHEST :
			case CRAFTING :
			case DROPPER :
			case ENDER_CHEST :
			case HOPPER :
			case PLAYER :
			case SHULKER_BOX :
			case WORKBENCH :
				return true;
			default :
				return false;
		}
	}
}
