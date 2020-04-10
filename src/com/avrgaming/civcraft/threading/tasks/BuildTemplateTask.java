package com.avrgaming.civcraft.threading.tasks;

import java.util.LinkedList;
import java.util.Queue;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.threading.sync.SyncBuildUpdateTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.SimpleBlock;

public class BuildTemplateTask implements Runnable {
	private Template tpl;
	private BlockCoord cornerBlock;

	private Queue<SimpleBlock> syncBlockQueue = new LinkedList<SimpleBlock>();
	int builtBlockCount = 0;

	private final int MAX_BLOCKS_PER_TICK = CivSettings.getIntBase("sync_build_update_task");
	private final int DELAY_SPEED = 20;

	public BuildTemplateTask(Template tpl, BlockCoord cornerBlock) {
		this.tpl = tpl;
		this.cornerBlock = cornerBlock;
	}

	private void oneLayer(boolean solidBlock, Template tpl, int y) throws InterruptedException {
		for (int x = 0; x < tpl.size_x; x++) {
			for (int z = 0; z < tpl.size_z; z++) {
				SimpleBlock sb = (solidBlock) ? new SimpleBlock(1, 0) : tpl.blocks[x][y][z];
				builtBlockCount++;
				/*
				 * We're resuming an undo task after reboot and this block is already built. Or
				 * This block is restricted
				 */
				if (CivSettings.restrictedUndoBlocks.contains(sb.getMaterial()))
					sb.setType(CivData.AIR);

				/* Convert relative template x,y,z to real x,y,z in world. */
				sb.x = x + cornerBlock.getX();
				sb.y = y + cornerBlock.getY();
				sb.z = z + cornerBlock.getZ();
				sb.worldname = cornerBlock.getWorldname();
				/* Add block to sync queue, will be built on next sync tick. */
				syncBlockQueue.add(sb);

				if (builtBlockCount > MAX_BLOCKS_PER_TICK) {
					/* Wait for a period of time. */
					Thread.sleep(DELAY_SPEED);
					SyncBuildUpdateTask.queueSimpleBlock(syncBlockQueue);
					syncBlockQueue.clear();
					builtBlockCount = 0;
				}
			}
		}
	}

	@Override
	public void run() {
		try {

			oneLayer(true, tpl, tpl.size_y - 1); // Для того что бы не сыпался песок сверху. Верхний слой устанавливается на
											// камень.
			for (int y = tpl.size_y - 2; y >= 0; y--) {
				oneLayer(false, tpl, y);
			}
			oneLayer(false, tpl, tpl.size_y - 1); // убераем верхний слой

			/* Build last remaining blocks. */
			SyncBuildUpdateTask.queueSimpleBlock(syncBlockQueue);
			syncBlockQueue.clear();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}