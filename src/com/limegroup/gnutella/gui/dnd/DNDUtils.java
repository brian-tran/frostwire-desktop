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

package com.limegroup.gnutella.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.limegroup.gnutella.util.URIUtils;

/**
 * Static helper class with DND tasks that provides methods for handling
 * URI and file drops and also provides default transfer handlers.
 */
public class DNDUtils {

    /**
     * Immutable list of default transfer handlers that should be chained
     * after a specific one.
     */
    public static final List<LimeTransferHandler> DEFAULT_TRANSFER_HANDLERS;

    /**
     * Default transfer handler supporting drops for all flavors limewire
     * is interested in. 
     */
    public static final LimeTransferHandler DEFAULT_TRANSFER_HANDLER;

    static {
        /*
         * The transfer handlers added here should not access the JComponent
         * given to them since it is null, as the global drop target installed
         * on limewire's JFrame is not a JComponent.
         * See TransferHandlerDropTargetListener to see how they are invoked
         */
        DEFAULT_TRANSFER_HANDLERS = Collections.unmodifiableList(Arrays.asList(new MagnetTransferHandler(), new TorrentURITransferHandler(),
                new TorrentFilesTransferHandler(), new SendFileTransferHandler()));

        DEFAULT_TRANSFER_HANDLER = new MulticastTransferHandler(DEFAULT_TRANSFER_HANDLERS);
    }

    /**
     * Returns array of uris extracted from transferable.
     * @param transferable
     * @return
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    public static URI[] getURIs(Transferable transferable) throws UnsupportedFlavorException, IOException {

        String lines = (String) (contains(transferable.getTransferDataFlavors(), FileTransferable.URIFlavor) ? transferable
                .getTransferData(FileTransferable.URIFlavor) : transferable.getTransferData(FileTransferable.URIFlavor16));

        StringTokenizer st = new StringTokenizer(lines, System.getProperty("line.separator"));
        ArrayList<URI> uris = new ArrayList<URI>();
        while (st.hasMoreTokens()) {
            String line = st.nextToken().trim();
            if (line.length() == 0) {
                continue;
            }
            try {
                URI uri = URIUtils.toURI(line);
                uris.add(uri);
            } catch (URISyntaxException e) {
                URIUtils.error(e);
            }
        }
        return uris.toArray(new URI[uris.size()]);
    }

    /**
     * Returns true if the flavor is contained in the array.
     * 
     * @param array
     * @param flavor
     * @return
     */
    public static boolean contains(DataFlavor[] array, DataFlavor flavor) {
        for (int i = 0; i < array.length; i++) {
            if (flavor.equals(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for {@link DataFlavor#javaFileListFlavor} and 
     * {@link FileTransferable#URIFlavor} for unix systems.  
     * @param flavors
     * @return
     */
    public static boolean containsFileFlavors(DataFlavor[] flavors) {
        if (flavors == null) {
            return false;
        }
        return contains(flavors, DataFlavor.javaFileListFlavor) || contains(flavors, FileTransferable.URIFlavor)
                || contains(flavors, FileTransferable.URIFlavor16);
    }

    /**
     * Extracts the array of files from a transferable
     * @param transferable
     * @return an empty array if the transferable does not contain any data
     * that can be interpreted as a list of files
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static File[] getFiles(Transferable transferable) throws UnsupportedFlavorException, IOException {
        if (contains(transferable.getTransferDataFlavors(), DataFlavor.javaFileListFlavor)) {
            return ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).toArray(new File[0]);
        } else if (contains(transferable.getTransferDataFlavors(), FileTransferable.URIFlavor)
                || contains(transferable.getTransferDataFlavors(), FileTransferable.URIFlavor16)) {
            return getFiles(getURIs(transferable));
        }
        return new File[0];
    }

    /**
     * Returns array of files for uris that denote local paths.
     * @param uris
     * @return empty array if no uri denotes a local file
     */
    public static File[] getFiles(URI[] uris) {
        ArrayList<File> files = new ArrayList<File>(uris.length);
        for (URI uri : uris) {
            String scheme = uri.getScheme();
            if (uri.isAbsolute() && scheme != null && scheme.equalsIgnoreCase("file")) {
                String path = uri.getPath();
                files.add(new File(path));
            }
        }
        return files.toArray(new File[files.size()]);
    }
}
