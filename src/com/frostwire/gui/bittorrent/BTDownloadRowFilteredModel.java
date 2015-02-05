/*
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

package com.frostwire.gui.bittorrent;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.transfers.TransferState;

import com.frostwire.gui.filters.TableLineFilter;
import com.limegroup.gnutella.settings.BittorrentSettings;

/**
 * Filters out certain rows from the data model.
 *
 * @author Sumeet Thadani
 * @author Sam Berlin
 * @author gubatron
 * @author aldenml
 *
 */
public class BTDownloadRowFilteredModel extends BTDownloadModel {

    /**
     * The filter to use in this row filter.
     */
    private final TableLineFilter<BTDownloadDataLine> FILTER;

    /**
     * A list of all filtered results.
     */
    protected final List<BTDownloadDataLine> HIDDEN;

    /**
     * Constructs a TableRowFilter with the specified TableLineFilter.
     */
    public BTDownloadRowFilteredModel(TableLineFilter<BTDownloadDataLine> f) {

        if (f == null)
            throw new NullPointerException("null filter");

        FILTER = f;
        HIDDEN = new ArrayList<BTDownloadDataLine>();
    }

    /**
     * Determines whether or not this line should be added.
     */
    public int add(BTDownloadDataLine tl, int row) {

        if (!allow(tl)) {
            HIDDEN.add(tl);
            return -1;
        } else {
            return super.add(tl, row);
        }

    }

    @Override
    public void clear() {
        super.clear();
        HIDDEN.clear();
    }

    /**
     * Notification that the filters have changed.
     */
    void filtersChanged() {
        rebuild();
        fireTableDataChanged();
    }

    /**
     * Determines whether or not the specified line is allowed by the filter.
     */
    private boolean allow(BTDownloadDataLine tl) {
        return FILTER.allow(tl);
    }

    /**
     * Rebuilds the internal map to denote a new filter.
     */
    private void rebuild() {
        List<BTDownloadDataLine> existing = new ArrayList<BTDownloadDataLine>(_list);
        List<BTDownloadDataLine> hidden = new ArrayList<BTDownloadDataLine>(HIDDEN);

        clear();

        // For stuff in _list, we can just re-add the DataLines as-is.
        for (int i = 0; i < existing.size(); i++) {
            //if (isSorted()) {
            //see override of getSortedPosition.
            //rebuild only seems to happen when we first build the table
            //in which case addSorted takes care of business by invoking getSortedPosition.
            addSorted(existing.get(i));
            //} else {
            //	add(existing.get(i));
            //	}
        }

        // Merge the hidden TableLines
        for (int i = 0; i < hidden.size(); i++) {
            BTDownloadDataLine tl = hidden.get(i);

            //if(isSorted()) {
            addSorted(tl);
            //} else {
            //    add(tl);
            // }
        }

    }

    @Override
    public void sort(int col) {
        super.sort(col);
        saveSortSettings();
    }

    @Override
    public void unsort() {
        super.unsort();
        saveSortSettings();
    }

    private void saveSortSettings() {
        BittorrentSettings.BTMEDIATOR_COLUMN_SORT_INDEX.setValue(getSortColumn());
        BittorrentSettings.BTMEDIATOR_COLUMN_SORT_ORDER.setValue(isSortAscending());
    }

    @Override
    int getActiveUploads() {
        int count = super.getActiveUploads();

        int size = HIDDEN.size();

        try {
            for (int i = 0; i < size; i++) {
                BTDownload downloader = HIDDEN.get(i).getInitializeObject();
                // special case for peer uploads, needs refactor
                if (downloader instanceof BTPeerHttpUpload) {
                    if (downloader.getState() == TransferState.SEEDING) {
                        count++;
                    }
                } else {
                    if (downloader.isCompleted() && downloader.getState() == TransferState.SEEDING) {
                        count++;
                    }
                }
            }
        } catch (Throwable e) {
            // ignore, multi-threading issues?
        }
        return count;
    }
}
