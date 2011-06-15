package com.limegroup.gnutella.gui;

import java.io.File;
import java.util.Set;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.limewire.io.IpPort;

import com.frostwire.bittorrent.BTDownloader;
import com.google.inject.Singleton;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.Downloader.DownloadStatus;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.browser.MagnetOptions;
import com.limegroup.gnutella.gui.search.SearchInformation;
import com.limegroup.gnutella.gui.search.SearchMediator;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.settings.QuestionsHandler;
import com.limegroup.gnutella.settings.iTunesSettings;
import com.limegroup.gnutella.util.QueryUtils;
import com.limegroup.gnutella.version.UpdateInformation;

/**
 * This class is the gateway from the backend to the frontend.  It
 * delegates all callbacks to the appropriate frontend classes, and it
 * also handles putting calls onto the Swing thread as necessary.
 * 
 * It implements the <tt>ActivityCallback</tt> callback interface, designed
 * to make it easy to swap UIs.
 */
@Singleton
public final class VisualConnectionCallback implements ActivityCallback {
	
	
	///////////////////////////////////////////////////////////////////////////
	//  Files-related callbacks
	///////////////////////////////////////////////////////////////////////////
	
    /** 
	 * This method notifies the frontend that the data for the 
	 * specified shared <tt>File</tt> instance has been 
	 * updated.
	 *
	 * @param file the <tt>File</tt> instance for the shared file whose
	 *  data has been updated
	 */
    public void handleSharedFileUpdate(final File file) {
        /**
         * NOTE: Pass this off directly to the library
         * so it can discard the update if the directory
         * of the file isn't selected.
         * This reduces the amount of Runnables created
         * by a very large amount.
         */
         mf().getLibraryMediator().updateSharedFile(file);
    }
    
    public void fileManagerLoading() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mf().getLibraryMediator().clearLibrary();
            }
        });
    }
    

	///////////////////////////////////////////////////////////////////////////
	//  Download-related callbacks
	///////////////////////////////////////////////////////////////////////////
	
    public void addDownload(Downloader mgr) {
//        Runnable doWorkRunnable = new AddDownload(mgr);
//        
//        if (mgr instanceof BTDownloaderImpl) {
//        	if (((BTDownloader) mgr).isCompleted()) {
//        		//don't add it visually
//        		return;
//        	}
//        }
//        
//        SwingUtilities.invokeLater(doWorkRunnable);
    }
    
    public void addDownload(BTDownloader mgr) {
        Runnable doWorkRunnable = new AddDownload(mgr);
        
        SwingUtilities.invokeLater(doWorkRunnable);
    }

//    public void removeDownload(Downloader mgr) {
//        Runnable doWorkRunnable = new RemoveDownload(mgr);
//        SwingUtilities.invokeLater(doWorkRunnable);
//        
//        if (iTunesSettings.ITUNES_SUPPORT_ENABLED.getValue() 
//                && mgr.getState() == DownloadStatus.COMPLETE) {
//            
//            iTunesMediator.instance().scanForSongs(mgr.getSaveFile());
//        }
//    }
    
    public void downloadsComplete() {
        Finalizer.setDownloadsComplete();
    }

	/**
	 *  Show active downloads
	 */
	public void showDownloads() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		        GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);	
            }
        });
	}	
    
    private class AddDownload implements Runnable {
        private BTDownloader mgr;
        public AddDownload(BTDownloader mgr) {
            this.mgr = mgr;
        }
        public void run() {
            mf().getBTDownloadMediator().add(mgr);
		}
    }

	
	///////////////////////////////////////////////////////////////////////////
	//  Upload-related callbacks
	///////////////////////////////////////////////////////////////////////////
    
    public void uploadsComplete() {
        Finalizer.setUploadsComplete();
    }
    	
	///////////////////////////////////////////////////////////////////////////
	//  Other stuff
	///////////////////////////////////////////////////////////////////////////
	
    /**
     * Notification that the address has changed.
     */
    public void handleAddressStateChanged() {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                // don't touch GUI code if it isn't constructed.
//                // this is necessary here only because addressStateChanged
//                // is triggered by Acceptor, which is init'd prior to the
//                // GUI actually existing.
//                if (GUIMediator.isConstructed())
//                    SearchMediator.addressChanged();
//            }
//        });
    }
    
    public void handleNoInternetConnection() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (GUIMediator.isConstructed())
                    GUIMediator.disconnected();
            }
        });
    }

	/**
     * Notification that a new update is available.
     */
    public void updateAvailable(UpdateInformation update) {
        GUIMediator.instance().showUpdateNotification(update);
    }

	/**
	 *  Tell the GUI to deiconify.
	 */  
	public void restoreApplication() {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
		        GUIMediator.restoreView();
            }
        });
		    
	}
	
	/**
	 * Notification of a component loading.
	 */
	public void componentLoading(final String component) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
                GUIMediator.setSplashScreenString(
                    I18n.tr(component));
            }
        });
    }       
	
	/**
	 * Indicates that the firewalled state of this has changed. 
	 */
	public void acceptedIncomingChanged(final boolean status) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIMediator.instance().getStatusLine().updateFirewallLabel(status);
			}
		});
	}
    
    /**
     * Returns the MainFrame.
     */
    private MainFrame mf() {
        return GUIMediator.instance().getMainFrame();
    }

	/**
	 * Returns true since we want to kick off the magnet downloads ourselves.
	 */
	public boolean handleMagnets(final MagnetOptions[] magnets) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boolean oneSearchStarted = false;
				for (int i = 0; i < magnets.length; i++) {
					// spawn search for keyword only magnet
					if (magnets[i].isKeywordTopicOnly() && !oneSearchStarted) {
						String query = QueryUtils.createQueryString
							(magnets[i].getKeywordTopic());
						SearchInformation info = 
							SearchInformation.createKeywordSearch
							(query, null, MediaType.getAnyTypeMediaType());
						if (SearchMediator.validateInfo(info) 
							== SearchMediator.QUERY_VALID) {
							oneSearchStarted = true;
							SearchMediator.triggerSearch(info);
						}
					}
					else {
						//DownloaderUtils.createDownloader(magnets[i]);
					}
				}
				if (magnets.length > 0) {
					GUIMediator.instance().setWindow(GUIMediator.Tabs.SEARCH);
				}
			}
		});
		return true;
	}
	
	public void handleTorrent(final File torrentFile) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIMediator.instance().openTorrent(torrentFile);
			}
		});
	}
   
    public void installationCorrupted() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUIMediator.showWarning(I18n.tr("<html><b>Your FrostWire may have been corrupted by a virus or trojan!</b><br><br>Please visit <a href=\"http://www.frostwire.com/download\">www.frostwire.com</a> and download the newest official version of FrostWire.</html>"));
            }
        });
    }

	@Override
	public void handleTorrentMagnet(final String request) {
		GUIMediator.instance().openTorrentURI(request);
	}
}