
package com.avrgaming.civcraft.endgame;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.avrgaming.civcraft.endgame.EndGameCondition;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.sessiondb.SessionEntry;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.wonders.StockExchange;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.war.War;

public class EndConditionEconomic
extends EndGameCondition {
    public static boolean check = false;
    int daysAfterStart;
    Date startDate = null;

    @Override
    public void onLoad() {
        this.daysAfterStart = Integer.valueOf(this.getString("days_after_start"));
        this.getStartDate();
    }

    private void getStartDate() {
        String key = "endcondition:economic:startdate";
        ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
        if (entries.isEmpty()) {
            this.startDate = new Date();
            CivGlobal.getSessionDB().add(key, "" + this.startDate.getTime(), 0, 0, 0);
        } else {
            long time = Long.valueOf(entries.get((int)0).value);
            this.startDate = new Date(time);
        }
    }

    private boolean isAfterStartupTime() {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(this.startDate);
        Calendar now = Calendar.getInstance();
        startCal.add(5, this.daysAfterStart);
        return now.after(startCal);
    }

    @Override
    public String getSessionKey() {
        return "endgame:economic";
    }

    @Override
    public boolean check(Civilization civ) {
        if (!this.isAfterStartupTime()) {
            return false;
        }
        Wonder stock = null;
        boolean hasStock = false;
        for (Town town : civ.getTowns()) {
            if (town.getMotherCiv() != null) continue;
            for (Wonder wonder : town.getWonders()) {
                if (!wonder.isActive() || !wonder.getConfigId().equals("w_stock_exchange")) continue;
                stock = wonder;
                hasStock = true;
                break;
            }
            if (!hasStock) continue;
            break;
        }
        if (!hasStock) {
            return false;
        }
        if (civ.isConquered()) {
            return false;
        }
        if (stock == null) {
            return false;
        }
        StockExchange stockExchange = (StockExchange)stock;
        int stockLevel = stockExchange.getLevel();
        if (stockLevel != 6) {
            return false;
        }
        check = true;
        War.time_declare_days = 1;
        return true;
    }

    @Override
    protected void onWarDefeat(Civilization civ) {
        this.onFailure(civ);
    }
}

