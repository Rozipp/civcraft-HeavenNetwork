/************************************************************************* AVRGAMING LLC __________________
 * 
 * [2013] AVRGAMING LLC All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property of AVRGAMING LLC and its suppliers, if any. The intellectual and technical concepts
 * contained herein are proprietary to AVRGAMING LLC and its suppliers and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is
 * obtained from AVRGAMING LLC. */
package com.avrgaming.civcraft.village;

import java.util.concurrent.locks.ReentrantLock;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.threading.CivAsyncTask;

public class VillageHourlyTick extends CivAsyncTask {

	@Override
	public void run() {
		for (Village village : CivGlobal.getVillages()) {
			ReentrantLock reentrantLock = village.locks.get("longhouse");
			if (reentrantLock.tryLock()) {
				try {
					village.processFirepoints();
					village.processLonghouse(this);
				} finally {
					reentrantLock.unlock();
				}
			}
		}
	}

}
