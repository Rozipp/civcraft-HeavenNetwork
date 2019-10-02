package com.avrgaming.civcraft.threading.timers;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.TrommelAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;

public class UpdateStructuresWithLock extends CivAsyncTask {
	public static ReentrantLock lock;

	@Override
	public void run() {
		if (!UpdateStructuresWithLock.lock.tryLock()) {
			return;
		}
		try {
			final Iterator<Map.Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
			while (iter.hasNext()) {
				final Structure struct = iter.next().getValue();
				if (!struct.isActive()) {
					continue;
				}
				try {
					if (struct.getUpdateEvent() != null && !struct.getUpdateEvent().equals("")) {
						if (struct.getUpdateEvent().equals("trommel_process")) {
							if (!CivGlobal.trommelsEnabled) {
								continue;
							}
							TaskMaster.asyncTask("trommel-" + struct.getCorner().toString(), new TrommelAsyncTask(struct), 0L);
						} else
							if (struct.getUpdateEvent().equals("quarry_process")) {
								if (!CivGlobal.quarriesEnabled) continue;
							}
					}
					struct.onUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (final Wonder wonder : CivGlobal.getWonders()) {
				wonder.onUpdate();
			}
		} finally {
			UpdateStructuresWithLock.lock.unlock();
		}
	}

	static {
		UpdateStructuresWithLock.lock = new ReentrantLock();
	}
}
