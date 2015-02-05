/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.StringSetting;

/**
 * @author gubatron
 * @author aldenml
 */
public class UpdateManagerSettings extends LimeProps {

    private UpdateManagerSettings() {
    }


    /**
     * Wether or not to show promotion overlays
     */
    public static BooleanSetting SHOW_PROMOTION_OVERLAYS = (BooleanSetting) FACTORY.createBooleanSetting("SHOW_PROMOTION_OVERLAYS", true).setAlwaysSave(true);

    public static BooleanSetting SHOW_FROSTWIRE_RECOMMENDATIONS = (BooleanSetting) FACTORY.createBooleanSetting("SHOW_FROSTWIRE_RECOMMENDATIONS", true).setAlwaysSave(true);

    /**
     * URL to feed the Slideshow with the promotional frostclick overlays
     */
    public static StringSetting OVERLAY_SLIDESHOW_JSON_URL = FACTORY.createStringSetting("OVERLAY_SLIDESHOW_JSON_URL", "http://update.frostwire.com/o2.php");
}
