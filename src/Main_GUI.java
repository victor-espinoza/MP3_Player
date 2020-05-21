import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import net.iharder.dnd.FileDrop;
import java.awt.*;
import java.util.Vector;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class Main_GUI extends javax.swing.JFrame {

   private final Vector<Vector<String>> libraryData;
   private final Vector<String> header;

   // Container that holds the controlsJPanel and libraryJPanel
   private final JPanel container = new JPanel(new BorderLayout());

   // JPanel to hold the displayed information
   private final JPanel controlsJPanel = new JPanel(new FlowLayout());
   private final JPanel controlsJTable = new JPanel(new BorderLayout());
   private final Library songLibrary = new Library();
   private DefaultMutableTreeNode libraryNode;
   private DefaultMutableTreeNode playlistNode;
   private DefaultMutableTreeNode baseNode;

   // JPanel to hold the Library and Playlist in a JTree
   private final JPanel playlistLibraryJPanel = new JPanel();
   private JTree playlistLibraryJTree;
   private FileInputStream FIS;
   private BufferedInputStream BIS;
   private static AdvancedPlayer player;
   
   //Song Information Variables
   private long pauseLocation;
   private long songLength;
   private long progressBarVal = 0;
   private boolean isPaused = false;
   private boolean albumColumnIsChecked = false;
   private boolean artistColumnIsChecked = false;
   private boolean yearColumnIsChecked = false;
   private boolean genreColumnIsChecked = false;
   private boolean commentColumnIsChecked = false;
   private boolean repeatSelected = false;
   private boolean shuffleSelected = false;
   private boolean selectedViaShuffle = false;
   private boolean progressChanged = false;
   private boolean externalSongOpened = false;
   private String currentSongPlaying;
   private String songPaused;
   private String openedSong;
   private boolean songIsOpen = false;
   private boolean validPlayOption = true;
   private boolean playRecentItemSelected = false;
   //Initialized to loading library data
   private static boolean loadTableData = false;

   private Thread playerThread;

   // JSlider to control the volume of the audio.
   private JSlider volumeJSlider;

   private JScrollPane libJScrollPane;
   private JTable libJTable;
   private final JTableHeader libJTableHeader;

   //Popup menu for JTable
   private JPopupMenu popUpMenu;
   private JPopupMenu headerPopUpMenu;
   private final JMenuItem deleteItem;
   private final JMenuItem addItem;
   private JCheckBoxMenuItem albumColumn, artistColumn, yearColumn,
    genreColumn, commentColumn, shuffleMenuItem, repeatMenuItem;
   private final JMenuItem deleteFromPlaylistItem;
   private final JMenu addToPlaylist;
   private final JMenu playRecentMenu;

   //Popup menu for JTree
   private JPopupMenu jTreePopUpMenu;
   private final JMenuItem deletePlaylist;
   private final JMenuItem createPlaylist;

   // Progress Bar
   private final JPanel progressJPanel = new JPanel(new FlowLayout());
   private final JProgressBar songProgressBar;
   private final JTextField incrementSongTimer;
   private final JTextField decrementSongTimer;

   // Timer variables to update the Progress Bar
   final int half_sec = 10;           // 1/100 second updates
   Timer timer = new Timer(half_sec, (ActionEvent e) -> {
      duration();
   });

    // Setups the controlsJPanel by adding the buttons.
   // Setups the libraryJPanel by adding the JTable.
   // Creates new form Main_GUI
   private Main_GUI() throws Exception {
      //get data from database
      libraryData = songLibrary.getSongsInLibrary();

      //create header for the table
      header = new Vector<String>();
      header.add("FILE");
      header.add("TRACK");
      header.add("SONG");
      header.add("ARTIST");
      header.add("ALBUM");
      header.add("RELEASE_YEAR");
      header.add("GENRE");
      header.add("COMMENTS");
      header.add("SONG_KEY");

      initComponents();

      // JButton variables
      JButton playJButton;
      JButton pauseJButton;
      JButton stopJButton;
      JButton forwardJButton;
      JButton backwardJButton;

      // Backward JButton setup 
      backwardJButton = new JButton();
      backwardJButton.setText("<<");
      backwardJButton.setPreferredSize(new Dimension(120, 50));
      backwardJButton.addActionListener((ActionEvent e) -> {
         backwardJButtonActionPerformed();
      }); // event handler called when backwardJButton is clicked

      // Play JButton setup
      playJButton = new JButton();
      playJButton.setText("►");
      playJButton.setPreferredSize(new Dimension(120, 50));
      playJButton.addActionListener((ActionEvent e) -> {
         playJButtonActionPerformed();
      }); // event handler called when playJButton is clicked

      // Pause JButton setup
      pauseJButton = new JButton();
      pauseJButton.setText("❚❚");
      pauseJButton.setPreferredSize(new Dimension(120, 50));
      pauseJButton.addActionListener((ActionEvent e) -> {
         pauseJButtonActionPerformed();
      }); // event handler called when pauseJButton is clicked

      // Stop JButton setup
      stopJButton = new JButton();
      stopJButton.setText("■");
      stopJButton.setPreferredSize(new Dimension(120, 50));
      stopJButton.addActionListener((ActionEvent e) -> {
         stopJButtonActionPerformed();
      }); // event handler called when stopJButton is clicked

      // Backward JButton setup
      forwardJButton = new JButton();
      forwardJButton.setText(">>");
      forwardJButton.setPreferredSize(new Dimension(120, 50));
      forwardJButton.addActionListener((ActionEvent e) -> {
         forwardJButtonActionPerformed();
      }); // event handler called when forwardJButton is clicked

      volumeJSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
      volumeJSlider.setBackground(Color.white);
      volumeJSlider.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            Line.Info source = Port.Info.SPEAKER;

            if (AudioSystem.isLineSupported(source)) {
               try {
                  Port outline = (Port) AudioSystem.getLine(source);
                  outline.open();
                  FloatControl volumeControl = (FloatControl) outline.
                   getControl(FloatControl.Type.VOLUME);
                  float volume;
                  if (volumeJSlider.getValue() != 0) {
                     volume = (float) volumeJSlider.getValue() / 275;
                  } //so we dont damage our ears
                  else 
                     volume = 0;
                  volumeControl.setValue(volume);
               } catch (LineUnavailableException ex) {
                  System.out.println("Something went wrong!");
               }
            }
         }//endofstateChanged
      });
      volumeJSlider.setPaintTicks(true);
      volumeJSlider.setMajorTickSpacing(20);
      volumeJSlider.setMinorTickSpacing(10);

      // Setup the controls JPanel
      controlsJPanel.add(backwardJButton);
      controlsJPanel.add(playJButton);
      controlsJPanel.add(pauseJButton);
      controlsJPanel.add(stopJButton);
      controlsJPanel.add(forwardJButton);
      controlsJPanel.add(volumeJSlider);
      controlsJPanel.setBackground(Color.white);

      addItem = new JMenuItem("Add To Library");
      addItem.addActionListener((ActionEvent e) -> {
         addMenuItemActionPerformed();
      });

      deleteItem = new JMenuItem("Delete From Library");
      deleteItem.addActionListener((ActionEvent e) -> {
         deleteMenuItemActionPerformed();
      });

      addToPlaylist = new JMenu("Add To Playlist");
      CreatePlaylistMenu();

      playRecentMenu = new JMenu("Play Recent");
      CreatePlayRecentMenu();
        // playRecentMenuPopulateList code here

      deleteFromPlaylistItem = new JMenuItem("Remove From Playlist");
      deleteFromPlaylistItem.addActionListener((ActionEvent e) -> {
         deleteFromPlaylistItemActionPerformed();
      });

      //Add table pop-up menu functionality
      popUpMenu.add(addItem);
      popUpMenu.add(deleteItem);
      popUpMenu.add(addToPlaylist);
      popUpMenu.add(deleteFromPlaylistItem);
      libJTable.setComponentPopupMenu(popUpMenu);

      //Add header pop-up menu functionality
      albumColumn.setUI(new StayOpenCheckBoxMenuItemUI());
      albumColumn.addActionListener((ActionEvent e) -> {
         columnSelectActionPerformed();
      });

      artistColumn.setUI(new StayOpenCheckBoxMenuItemUI());
      artistColumn.addActionListener((ActionEvent e) -> {
         columnSelectActionPerformed();
      });

      yearColumn.setUI(new StayOpenCheckBoxMenuItemUI());
      yearColumn.addActionListener((ActionEvent e) -> {
         columnSelectActionPerformed();
      });

      genreColumn.setUI(new StayOpenCheckBoxMenuItemUI());
      genreColumn.addActionListener((ActionEvent e) -> {
         columnSelectActionPerformed();
      });

      commentColumn.setUI(new StayOpenCheckBoxMenuItemUI());
      commentColumn.addActionListener((ActionEvent e) -> {
         columnSelectActionPerformed();
      });

      headerPopUpMenu.add(artistColumn);
      headerPopUpMenu.add(albumColumn);
      headerPopUpMenu.add(yearColumn);
      headerPopUpMenu.add(genreColumn);
      headerPopUpMenu.add(commentColumn);

      libJTableHeader = libJTable.getTableHeader();
      libJTableHeader.add(headerPopUpMenu);
      libJTableHeader.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me))
               headerPopUpMenu.show(me.getComponent(), me.getX(), me.getY());
         }
      });

      // Setup the library JPanel
      libJTable.setPreferredSize(new Dimension(702, 500));
      //controlsJTable.add(libJTableHeader);
      JScrollPane libPane = new JScrollPane(libJTable);
      libPane.setBackground(Color.LIGHT_GRAY);
      controlsJTable.add(libPane);
      controlsJTable.setBackground(Color.white);

      //Setup the JTree JPanel
      playlistLibraryJPanel.setBackground(Color.white);
      JScrollPane scrollPane = new JScrollPane(playlistLibraryJTree);
      scrollPane.setPreferredSize(new Dimension(105, 495));
      playlistLibraryJPanel.add(scrollPane);
      playlistLibraryJPanel.setPreferredSize(new Dimension(115, 500));

      deletePlaylist = new JMenuItem("Delete Playlist(s)");
      deletePlaylist.addActionListener((ActionEvent e) -> {
         deletePlaylistMenuItemActionPerformed();
      });

      createPlaylist = new JMenuItem("Create Playlist");
      createPlaylist.addActionListener((ActionEvent e) -> {
         addPlaylistMenuItemActionPerformed();
      });

      //Add table pop-up menu 
      jTreePopUpMenu.add(createPlaylist);
      jTreePopUpMenu.add(deletePlaylist);
      playlistLibraryJTree.setComponentPopupMenu(jTreePopUpMenu);

      playlistLibraryJTree.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent me) {
            TreePath tp = playlistLibraryJTree.getPathForLocation(me.getX(),
             me.getY());
            if (tp != null) {
               DefaultMutableTreeNode currentNode
                = (DefaultMutableTreeNode) (tp.getLastPathComponent());
               String stringForm = currentNode.toString();
               if (stringForm.equals("Library") || stringForm.
                equals("Playlist")) {
                  loadTableData = false;
               } else if (!stringForm.equals("Playlist") && !stringForm.
                equals("Library")) {
                  loadTableData = true;
               }//end else
               Update_table();
            }//end if
         }//end if
      });

      // Setup progress bar
      songProgressBar = new JProgressBar();
      songProgressBar.setPreferredSize(new Dimension(690, 20));

      songProgressBar.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (player != null) {

               //Get mouse position (where user clicked on progress bar)
               int mousePos = e.getX();

               //Update the starting value to reflect the appropriate
               //percentage (so we know where to start playing the song)
               long newStartVal = (long) Math.round(((double) mousePos /(double)
                songProgressBar.getWidth()) * songProgressBar.getMaximum());

               //used to prevent data loss when casting a long to an int
               int startValInt = (int) Math.round(((double) mousePos / (double)
                songProgressBar.getWidth()) * songProgressBar.getMaximum());

               progressBarVal = newStartVal;
               progressChanged = true;

               updateTimerValues((double) newStartVal);
               player.close();
               timer.stop();
               if (!isPaused) {
                  playJButtonActionPerformed();
                  progressChanged = false;
                  progressBarVal = 0;
               }//end if
               else
                  songProgressBar.setValue(startValInt);
            }//end if
         }//close mouseClicked(MouseEvent e)
      });

      incrementSongTimer = new JTextField("00:00:00");
      incrementSongTimer.setPreferredSize(new Dimension(60, 20));
      incrementSongTimer.setEditable(false);
      incrementSongTimer.setHorizontalAlignment(JTextField.CENTER);
      incrementSongTimer.setBackground(Color.white);
      decrementSongTimer = new JTextField("--:--:--");
      decrementSongTimer.setPreferredSize(new Dimension(60, 20));
      decrementSongTimer.setEditable(false);
      decrementSongTimer.setHorizontalAlignment(JTextField.CENTER);
      decrementSongTimer.setBackground(Color.white);
      progressJPanel.add(incrementSongTimer);
      progressJPanel.add(songProgressBar);
      progressJPanel.add(decrementSongTimer);
      progressJPanel.setBackground(Color.white);

      // Adds the control and library JPanel to the container
      container.add(controlsJPanel, BorderLayout.PAGE_START);
      container.add(playlistLibraryJPanel, BorderLayout.LINE_START);
      container.add(controlsJTable, BorderLayout.CENTER);
      container.add(progressJPanel, BorderLayout.PAGE_END);

      new FileDrop(libJTable, (java.io.File[] files) -> {
         for (File file : files) {
            Mp3File song;
            try {
               song = new Mp3File(file.getAbsolutePath());
               //Object data2 = (Object)libJTable.getValueAt(deleteRow, 8);
               //songLibrary.removeDeletedSongsFromPlaylists(data2);
               boolean songExists = songLibrary.checkIfSongExistsInLibrary(
                file.getAbsolutePath());
               if (!loadTableData)
                  songLibrary.addSongToLibrary(song);
               else {
                  if (!songExists)
                     songLibrary.addSongToLibrary(song);
                  TreePath path = playlistLibraryJTree.getSelectionPath();
                  if (path != null) {
                     DefaultMutableTreeNode deleteNode
                      = (DefaultMutableTreeNode) (path.getLastPathComponent());
                     String stringForm = deleteNode.toString();
                     int playlistKey = songLibrary.getPlaylist(stringForm);
                     if (!stringForm.equals("Library")
                      && !stringForm.equals("Playlist")) {
                        Object songKey = songLibrary.
                         getLibrarySongId(file.getAbsolutePath());
                        if (songKey != null)
                           songLibrary.addSongToPlaylist(playlistKey, songKey);
                     }//end if
                  }//end if                         
               }//end else                    
            } catch (IOException | UnsupportedTagException |
             InvalidDataException ex) {
               Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE,
                null, ex);
            }//end catch
         }//end for
         Update_table();
      }); // end FileDrop.Listener

      KeyStroke controlI = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.
       CTRL_MASK);
      KeyStroke controlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.
       CTRL_MASK);
      KeyStroke controlRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
       InputEvent.CTRL_MASK);
      KeyStroke controlLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
       InputEvent.CTRL_MASK);
      KeyStroke controlL = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.
       CTRL_MASK);
      KeyStroke spacebar = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false);

      container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlD,
       "Decrease Volume");
      container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlI,
       "Increase Volume");
      container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlRight,
       "Next Song");
      container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlLeft,
       "Previous Song");
      container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(controlL,
       "Current Song");

      container.getActionMap().put("Increase Volume", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            increaseVolumeMenuItemActionPerformed();
         }
      });

      container.getActionMap().put("Decrease Volume", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            decreaseVolumeMenuItemActionPerformed();
         }
      });

      container.getActionMap().put("Next Song", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            forwardJButtonActionPerformed();
         }
      });

      container.getActionMap().put("Previous Song", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            backwardJButtonActionPerformed();
         }
      });

      container.getActionMap().put("Current Song", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            goToCurrentMenuItemActionPerformed();
         }
      });

   }

   // Duration calculations

   private void duration() {
      try {
         File file = new File(currentSongPlaying);
         AudioFileFormat baseFileFormat = new MpegAudioFileReader()
          .getAudioFileFormat(file);
         Map properties = baseFileFormat.properties();
         if (player != null) {
            Long duration = (Long) properties.get("duration");
            double millisecond = (double) (duration / 1000);

            double max = songLength;
            double current = max - FIS.available();
            double percentage = current / max;

            double countUpTimer = (percentage * millisecond);
            double countDownTimer = millisecond - (percentage * millisecond);

            incrementSongTimer.setText(displayTime(countUpTimer));
            songProgressBar.setMaximum((int) max);
            songProgressBar.setValue((int) current);
            decrementSongTimer.setText(displayTime(countDownTimer));
         }

      } catch (UnsupportedAudioFileException | IOException ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }
   }

   private void updateTimerValues(double current) {
      try {
         File file = new File(currentSongPlaying);
         AudioFileFormat baseFileFormat = new MpegAudioFileReader()
          .getAudioFileFormat(file);
         Map properties = baseFileFormat.properties();
         if (player != null) {
            Long duration = (Long) properties.get("duration");
            double millisecond = (double) (duration / 1000);

            double max = songLength;
            //double current = max - FIS.available();
            double percentage = current / max;

            double countUpTimer = (percentage * millisecond);
            double countDownTimer = millisecond - (percentage * millisecond);

            incrementSongTimer.setText(displayTime(countUpTimer));
            songProgressBar.setMaximum((int) max);
            songProgressBar.setValue((int) current);
            decrementSongTimer.setText(displayTime(countDownTimer));
         }//end if
      } catch (UnsupportedAudioFileException | IOException ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }//end catch
   }//close updateTimerValues(...)

   private String displayTime(double millisecond) {
      int second = (int) (millisecond / 1000) % 60;
      int minute = (int) (millisecond / 1000) / 60 % 60;
      int hour = minute / 60;

      return String.format("%02d", hour) + ":"
       + String.format("%02d", minute) + ":"
       + String.format("%02d", second);
   }

   // Calls the forward method from the library class
   private void forwardJButtonActionPerformed() {   
      /* insert method from library to skip to next audio file */
      int row = libJTable.getSelectedRow();
      if (row >= 0 && !externalSongOpened) {
         if (row < libJTable.convertRowIndexToView(libJTable.getRowCount()-1)){
            libJTable.setRowSelectionInterval(libJTable.getSelectedRow() + 1,
             libJTable.getSelectedRow() + 1);
            libJTable.scrollRectToVisible(new Rectangle(
             libJTable.getCellRect(libJTable.getSelectedRow() + 1,
             libJTable.getSelectedRow() + 1, true)));
         }//end if
         if (row == libJTable.convertRowIndexToView(libJTable.getRowCount()-1)){
            libJTable.setRowSelectionInterval(0, 0);
            libJTable.scrollRectToVisible(new Rectangle(
             libJTable.getCellRect(0, 0, true)));
         }//end if
         isPaused = false;
         validPlayOption = true;
         progressChanged = false;
         selectedViaShuffle = false;
         container.repaint();
         timer.start();
         playJButtonActionPerformed(); 
      }//end if
      //timer.start();
   }

   // plays the audio file
   private void playJButtonActionPerformed() {   
      /* insert code to play the audio file */
      try {
         int row = libJTable.getSelectedRow();
         if (row < 0 && currentSongPlaying == null && !shuffleSelected
          && !playRecentItemSelected && !externalSongOpened) {
            libJTable.setRowSelectionInterval(0, 0);
            row = libJTable.getSelectedRow();
            libJTable.scrollRectToVisible(new Rectangle(
             libJTable.getCellRect(0, 0, true)));
            selectedViaShuffle = false;
         } else if (row < 0 && currentSongPlaying == null && !repeatSelected
          && shuffleSelected && !playRecentItemSelected) {
            int randomRow = libJTable.getRowCount();
            Random generator = new Random();
            row = generator.nextInt(randomRow);
            libJTable.setRowSelectionInterval(row, row);
            selectedViaShuffle = true;
         }//end else
         if (row >= 0 || (openedSong != null && songIsOpen)
          || playRecentItemSelected) {
            if (row >= 0) {
               libJTable.scrollRectToVisible(new Rectangle(
                libJTable.getCellRect(row, row, true)));
               Object data = (Object) libJTable.getValueAt(row, 0);
               if (currentSongPlaying != null && !currentSongPlaying.equals(
                (String) data)) {
                  timer.stop();
                  validPlayOption = true;
               }//end if
               currentSongPlaying = (String) data;
               if (!externalSongOpened)
                  songIsOpen = false;           

               if (!selectedViaShuffle && !isPaused && validPlayOption) {
                  if (songLibrary.getRecentlyPlayedSongsNumber() > 9) 
                     songLibrary.removeSongFromRecentlyPlayed();
                  String fileLocation = (String) data;
                  Object data1 = (Object) libJTable.getValueAt(row, 2);
                  String songName = (String) data1;
                  songLibrary.updateRecentlyPlayedSongs(fileLocation, songName);
               }
            }//end if
            if (openedSong != null && songIsOpen) 
               currentSongPlaying = openedSong; 
            if (player != null && validPlayOption) 
               player.close();
            if (progressChanged)
               validPlayOption = true;
            if (!validPlayOption)
               return;

            FIS = new FileInputStream(currentSongPlaying);
            BIS = new BufferedInputStream(FIS);
            player = new AdvancedPlayer(BIS);
            player.setPlayBackListener(new PlaybackListener() {
               @Override
               public void playbackFinished(PlaybackEvent event) {
                  stopJButtonActionPerformed();
                  if (repeatSelected || shuffleSelected) {
                     if (shuffleSelected && !repeatSelected) {
                        selectedViaShuffle = true;
                        int randomRow = libJTable.getRowCount();
                        Random generator = new Random();
                        int newRow = generator.nextInt(randomRow);
                        libJTable.setRowSelectionInterval(newRow, newRow);
                     }//end if
                     playJButtonActionPerformed();
                  }//end if
                  else
                     forwardJButtonActionPerformed();
               }
            });
            if (progressChanged)
               FIS.skip(progressBarVal);
            else if (isPaused && songPaused != null && currentSongPlaying
             != null && songPaused.equals(currentSongPlaying)) 
               FIS.skip(pauseLocation);
            else 
               songLength = FIS.available();
            
            isPaused = false;
            timer.start();

         }//end if
      }//end try
      catch (FileNotFoundException | JavaLayerException ex) {
         System.out.println(ex);
      } catch (IOException ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }

      playerThread = new Thread() {
         @Override
         public void run() {
            try {
               if (player != null && validPlayOption) {
                  validPlayOption = false;
                  player.play();
               }//end if
            } catch (JavaLayerException ex) {
               Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE,
                null, ex);
            }//end catch
         }
      };
      playerThread.start();
      selectedViaShuffle = false;
      CreatePlayRecentMenu();

   }

   // Calls the pause method from the library class
   private void pauseJButtonActionPerformed() {   
      /* insert method from library to pause the audio file */

      if (player != null && !isPaused) {
         try {
            pauseLocation = songLength - FIS.available();
            player.close();
            isPaused = true;
            validPlayOption = true;
            songPaused = currentSongPlaying;
         } catch (IOException ex) {
            Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE,
             null, ex);
         }
      }
      timer.stop();
   }

   // Calls the stop method from the library class
   private void stopJButtonActionPerformed() {   
      /* insert method from library to stop the audio file */

      if (player != null) {
         player.close();
         songLength = 0;
         pauseLocation = 0;
         isPaused = false;
         progressChanged = false;
         validPlayOption = true;
         currentSongPlaying = null;
         selectedViaShuffle = false;
         playRecentItemSelected = false;
         externalSongOpened = false;
         player = null;
      }//end if
      timer.stop();
      incrementSongTimer.setText("00:00:00");
      decrementSongTimer.setText("--:--:--");
      songProgressBar.setValue(0);
   }

   // Calls the backward method from the library class
   private void backwardJButtonActionPerformed() {
      int row = libJTable.getSelectedRow();
      if (row >= 0 && !externalSongOpened) {
         if (row > 0) {
            libJTable.setRowSelectionInterval(libJTable.getSelectedRow() - 1,
             libJTable.getSelectedRow() - 1);
            libJTable.scrollRectToVisible(new Rectangle(
             libJTable.getCellRect(libJTable.getSelectedRow() - 1, 
             libJTable.getSelectedRow() - 1, true)));
         }//end if
         if (row == 0) {
            libJTable.setRowSelectionInterval(libJTable.
             convertRowIndexToView(libJTable.getRowCount() - 1),
             libJTable.convertRowIndexToView(libJTable.getRowCount() - 1));
            libJTable.scrollRectToVisible(new Rectangle(
             libJTable.getCellRect(libJTable.convertRowIndexToView(
             libJTable.getRowCount() - 1), libJTable.convertRowIndexToView(
             libJTable.getRowCount() - 1), true)));
         }//end if
         isPaused = false;
         validPlayOption = true;
         selectedViaShuffle = false;
         progressChanged = false;
         container.repaint();
         timer.start();
         playJButtonActionPerformed();
      }//end if
      //timer.start();
   }

   // Menubar method
   private JMenuBar createMenuBar() {
      // Menu bar variables
      JMenuBar menuBar;
      JMenu fileMenu, controlMenu;
      JMenuItem addMenuItem, deleteMenuItem, openMenuItem, exitMenuItem,
       addPLMenuItem, deletePLMenuItem, playMenuItem, nextMenuItem,
       previousMenuItem, goToCurrentMenuItem, increaseVolumeMenuItem,
       decreaseVolumeMenuItem;

      // Creates the menu bar
      menuBar = new JMenuBar();

      // Builds the dropdown menus
      fileMenu = new JMenu("File");
      controlMenu = new JMenu("Control");

      // Individual selections for the dropdown menus
      openMenuItem = new JMenuItem("Open MP3");
      openMenuItem.addActionListener((ActionEvent e) -> {
         openMenuItemActionPerformed();
      });

      addMenuItem = new JMenuItem("Add MP3");
      addMenuItem.addActionListener((ActionEvent e) -> {
         addMenuItemActionPerformed();
      });

      deleteMenuItem = new JMenuItem("Delete MP3");
      deleteMenuItem.addActionListener((ActionEvent e) -> {
         deleteMenuItemActionPerformed();
      });

      addPLMenuItem = new JMenuItem("Create Playlist");
      addPLMenuItem.addActionListener((ActionEvent e) -> {
         addPlaylistMenuItemActionPerformed();
      });

      deletePLMenuItem = new JMenuItem("Delete Playlist");
      deletePLMenuItem.addActionListener((ActionEvent e) -> {
         deletePlaylistMenuItemActionPerformed();
      });

      exitMenuItem = new JMenuItem("Exit");
      exitMenuItem.addActionListener((ActionEvent e) -> {
         System.exit(0);
      });

      playMenuItem = new JMenuItem("Play");
      playMenuItem.addActionListener((ActionEvent e) -> {
         playJButtonActionPerformed();
      });

      nextMenuItem = new JMenuItem("Next");
      nextMenuItem.addActionListener((ActionEvent e) -> {
         forwardJButtonActionPerformed();
      });

      previousMenuItem = new JMenuItem("Previous");
      previousMenuItem.addActionListener((ActionEvent e) -> {
         backwardJButtonActionPerformed();
      });

      goToCurrentMenuItem = new JMenuItem("Go to Current Song");
      goToCurrentMenuItem.addActionListener((ActionEvent e) -> {
         goToCurrentMenuItemActionPerformed();
      });

      increaseVolumeMenuItem = new JMenuItem("Increase Volume");
      increaseVolumeMenuItem.addActionListener((ActionEvent e) -> {
         increaseVolumeMenuItemActionPerformed();
      });

      decreaseVolumeMenuItem = new JMenuItem("Decrease Volume");
      decreaseVolumeMenuItem.addActionListener((ActionEvent e) -> {
         decreaseVolumeMenuItemActionPerformed();
      });

      shuffleMenuItem = new JCheckBoxMenuItem("Shuffle");
      shuffleMenuItem.addActionListener((ActionEvent e) -> {
         shuffleMenuItemActionPerformed();
      });

      repeatMenuItem = new JCheckBoxMenuItem("Repeat");
      repeatMenuItem.addActionListener((ActionEvent e) -> {
         repeatMenuItemActionPerformed();
      });

      // Adds the selection options to the File menu.
      fileMenu.add(openMenuItem);
      fileMenu.addSeparator();
      fileMenu.add(addMenuItem);
      fileMenu.add(deleteMenuItem);
      fileMenu.addSeparator();
      fileMenu.add(addPLMenuItem);
      fileMenu.add(deletePLMenuItem);
      fileMenu.addSeparator();
      fileMenu.add(exitMenuItem);

      // Adds the selection options to the Control menu.
      controlMenu.add(playMenuItem);
      controlMenu.add(nextMenuItem);
      controlMenu.add(previousMenuItem);
      controlMenu.add(playRecentMenu);
      controlMenu.add(goToCurrentMenuItem);
      controlMenu.addSeparator();
      controlMenu.add(increaseVolumeMenuItem);
      controlMenu.add(decreaseVolumeMenuItem);
      controlMenu.addSeparator();
      controlMenu.add(shuffleMenuItem);
      controlMenu.add(repeatMenuItem);

      // Adds menu to menubar
      menuBar.add(fileMenu);
      menuBar.add(controlMenu);

      return menuBar;
   }

    // Method to 
   // Method to go to the current/selected song on the playlist.
   private void goToCurrentMenuItemActionPerformed() {
      boolean foundRow = false;

        //First make sure that the playing song is not located within the
      //playlist. If it is located in the playlist, then select the song from
      //there. 
      if (player != null && !foundRow) {
         int totalRows = libJTable.getRowCount();
         for (int row = 0; row < totalRows; row++) {
            Object data = (Object) libJTable.getValueAt(row, 0);
            if (currentSongPlaying != null && currentSongPlaying.equals(
             (String) data)) {
               libJTable.setRowSelectionInterval(row, row);
               libJTable.scrollRectToVisible(new Rectangle(
                libJTable.getCellRect(row, row, true)));
               foundRow = true;
               break; 
            }//end if
         }//end for
      }//end if  

      //highlight the currently playing song and scroll the screen to insure 
      //that song is visible.
      if (player != null && !foundRow && !songIsOpen) {
         loadTableData = false;
         Update_table();
         int totalRows = libJTable.getRowCount();
         for (int row = 0; row < totalRows; row++) {
            Object data = (Object) libJTable.getValueAt(row, 0);
            if (currentSongPlaying != null && currentSongPlaying.equals(
             (String) data)) {
               libJTable.setRowSelectionInterval(row, row);
               libJTable.scrollRectToVisible(new Rectangle(
                libJTable.getCellRect(row, row, true)));
               foundRow = true;
               break;
            }//end if
         }//end for
      }//end if 

      //If there is no currently playing song, but a row is 
      //selected, then go to the selected row.
      int currentRow = libJTable.getSelectedRow();
      if ((player == null || (player != null && songIsOpen))
       && !foundRow && currentRow >= 0) {
         libJTable.setRowSelectionInterval(currentRow, currentRow);
         libJTable.scrollRectToVisible(new Rectangle(
          libJTable.getCellRect(currentRow, currentRow, true)));
      }//end if 

       //If no song is selected, do nothing.    
   }

   // Method to increase the volume in increments of 5%. 
   private void increaseVolumeMenuItemActionPerformed() {
      double tempVolume = volumeJSlider.getValue();
      tempVolume += 5;
      volumeJSlider.setValue((int) tempVolume);
   }

   // Method to decrease the volume in increments of 5%.
   private void decreaseVolumeMenuItemActionPerformed() {
      double tempVolume = volumeJSlider.getValue();
      tempVolume -= 5;
      volumeJSlider.setValue((int) tempVolume);
   }

   // JFileChooser method to select an audio file to add to the library
   private void addMenuItemActionPerformed() {
      // Create a file chooser
      int currentRow = libJTable.getSelectedRow();
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files",
       "mp3"));

      fileChooser.setMultiSelectionEnabled(true);
      int returnVal = fileChooser.showOpenDialog(Main_GUI.this);
      File[] musicFiles = fileChooser.getSelectedFiles();
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         for (File file : musicFiles) {
            try {
               /* Code to call the method to add file to the library 
                database getSelectedFile() */
               Mp3File song = new Mp3File(file.getAbsolutePath());
               songLibrary.addSongToLibrary(song);
            }//end try
            catch (IOException | InvalidDataException |
             UnsupportedTagException ex) {
               Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE,
                null, ex);
            }//end catch
         }//end for loop
      }//end if statement
      Update_table();
      if (currentRow >= 0) {
         libJTable.setRowSelectionInterval(currentRow, currentRow);
         libJTable.scrollRectToVisible(new Rectangle(
          libJTable.getCellRect(currentRow, currentRow, true)));
      }//end if  
   }

   // Calls method to delete a selected audio file from the library
   private void deleteMenuItemActionPerformed() {
      int[] rows = libJTable.getSelectedRows();
      if (rows.length > 0) {
         for (int deleteRow : rows) {
            Object data = (Object) libJTable.getValueAt(deleteRow, 0);
            if (currentSongPlaying != null && currentSongPlaying.equals(
             (String) data)) {
               stopJButtonActionPerformed();
            }//end if
            //remove from playlistSongs first, then remove from library
            Object data2 = (Object) libJTable.getValueAt(deleteRow, 8);
            songLibrary.removeDeletedSongsFromPlaylists(data2);
            songLibrary.removeSongFromLibrary(data2);
         }//end for
      }//end if
      Update_table();
   }

   private void openMenuItemActionPerformed() {
      // Create a file chooser
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files",
       "mp3"));

      int returnVal = fileChooser.showOpenDialog(Main_GUI.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         openedSong = (String) fileChooser.getSelectedFile().getAbsolutePath();
         songIsOpen = true;
         validPlayOption = true;
         int row = libJTable.getSelectedRow();
         if (row >= 0)
            libJTable.removeRowSelectionInterval(row, row);
         externalSongOpened = true;
         playJButtonActionPerformed();
      }//end if
   }//close openMenuItemActionPerformed()

   private void addPlaylistMenuItemActionPerformed() {
      boolean exitCondition = false;
      try {
         while(!exitCondition) {
            JFrame addNewPlaylistPopUpMenu = new JFrame();
            // prompt the user to enter the desired playlist name
            String name = JOptionPane.showInputDialog(
             addNewPlaylistPopUpMenu, "Please Enter A Playlist Name", null);
            // get the user's input. note that if they press Cancel, 
            //'name' will be null
            exitCondition = songLibrary.getPlaylistNameAvailability(name);
            if (!exitCondition) {
               //The playlist name already exists in the playlist table
               if (!name.equals("Library") && !name.equals("Playlists")) {
                  JOptionPane.showMessageDialog(addNewPlaylistPopUpMenu,
                   "Playlist name already exists! Please choose another name:");
               } else {
                  JOptionPane.showMessageDialog(addNewPlaylistPopUpMenu,
                   "Invalid Playlist Name! Please choose another name:");
               }//end else
            } else {
               if (name != null && !name.equals("")) {
                  songLibrary.addNewPlaylist(name);
                  DefaultTreeModel model = (DefaultTreeModel) 
                   playlistLibraryJTree.getModel();
                  DefaultMutableTreeNode newNode = new 
                   DefaultMutableTreeNode(name);
                  playlistNode.add(newNode);
                  model.reload(playlistNode);
                  loadTableData = true;
                  CreatePlaylistMenu();
                  playlistLibraryJTree.setSelectionPath(new TreePath(
                   newNode.getPath()));
                  Update_table();
               }//end if
            }//end else
         }
      } catch (Exception ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }//end catch

   }

   private void deletePlaylistMenuItemActionPerformed() {
      // Get paths of all selected nodes
      TreePath[] paths = playlistLibraryJTree.getSelectionPaths();
      if (paths != null) {
         DefaultTreeModel model = (DefaultTreeModel) (playlistLibraryJTree.
          getModel());
         DefaultMutableTreeNode deleteNode;
         for (TreePath path : paths) {
            deleteNode = (DefaultMutableTreeNode) (path.getLastPathComponent());
            String stringForm = deleteNode.toString();
            if (!stringForm.equals("Library") && 
             !stringForm.equals("Playlist")) {
               int x = songLibrary.getPlaylist(stringForm);
               songLibrary.removePlaylistSongsRow(x);
               songLibrary.removePlaylist(stringForm);
               model.removeNodeFromParent(deleteNode);
               loadTableData = false;
               Update_table();
               CreatePlaylistMenu();
            }
         }
      }
   }

   private void CreatePlaylistMenu() {
      addToPlaylist.removeAll();
      Enumeration en = playlistNode.breadthFirstEnumeration();
      if (en.hasMoreElements())
         en.nextElement();
      while (en.hasMoreElements()) {
         DefaultMutableTreeNode node= (DefaultMutableTreeNode) en.nextElement();
         String stringForm = node.toString();
         JMenuItem newItem = new JMenuItem(stringForm);
         newItem.addActionListener((ActionEvent e) -> {
            newItem.getText();
            int x = songLibrary.getPlaylist(newItem.getText());
            int[] rows = libJTable.getSelectedRows();
            if (rows.length > 0) {
               for (int desiredRow : rows) {
                  Object data = (Object) libJTable.getValueAt(desiredRow, 8);
                  songLibrary.addSongToPlaylist(x, data);
               }//end for
            }//end if
         });
         addToPlaylist.add(newItem);
      }
   }

   private void CreatePlayRecentMenu() {
      playRecentMenu.removeAll();
      ArrayList<String> songs = songLibrary.getRecentlyPlayedSongs();
      for (String s : songs) {
         JMenuItem newItem = new JMenuItem(s);
         //newItem.setUI(new StayOpenMenuItemUI());
         newItem.addActionListener((ActionEvent e) -> {
            playRecentItemSelected = true;
            validPlayOption = true;
            currentSongPlaying = (String) songLibrary.getPlayRecentSong(s);
            libJTable.getSelectionModel().clearSelection();
            if (songLibrary.getRecentlyPlayedSongsNumber() > 9)
               songLibrary.removeSongFromRecentlyPlayed();
            songLibrary.updateRecentlyPlayedSongs(currentSongPlaying, s);
            playJButtonActionPerformed();
         });
         playRecentMenu.add(newItem);
      }
   }

   private void repeatMenuItemActionPerformed() {
      repeatSelected = repeatMenuItem.isSelected();
   }

   private void shuffleMenuItemActionPerformed() {
      shuffleSelected = shuffleMenuItem.isSelected();
      if (shuffleSelected && !repeatSelected && player == null) {
         selectedViaShuffle = true;
         int randomRow = libJTable.getRowCount();
         Random generator = new Random();
         int newRow = generator.nextInt(randomRow);
         libJTable.setRowSelectionInterval(newRow, newRow);
         playJButtonActionPerformed();
      }//end if
      if (!shuffleSelected)
         selectedViaShuffle = false;
   }

   private void columnSelectActionPerformed() {
      albumColumnIsChecked = albumColumn.isSelected();
      artistColumnIsChecked = artistColumn.isSelected();
      yearColumnIsChecked = yearColumn.isSelected();
      genreColumnIsChecked = genreColumn.isSelected();
      commentColumnIsChecked = commentColumn.isSelected();

      songLibrary.removeColumnDisplayTableRow();
      songLibrary.updateDisplayedColumns(albumColumnIsChecked,
       artistColumnIsChecked, yearColumnIsChecked, genreColumnIsChecked,
       commentColumnIsChecked);

      Update_table();
      goToCurrentMenuItemActionPerformed();
   }

   private void deleteFromPlaylistItemActionPerformed() {
      TreePath path = playlistLibraryJTree.getSelectionPath();
      if (path != null) {
         DefaultTreeModel model = (DefaultTreeModel) (playlistLibraryJTree.
          getModel());
         DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) (path.
          getLastPathComponent());
         String stringForm = deleteNode.toString();
         if (!stringForm.equals("Library") && !stringForm.equals("Playlist")) {
            int x = songLibrary.getPlaylist(stringForm);
            int[] rows = libJTable.getSelectedRows();
            if (rows.length > 0) {
               for (int desiredRow : rows) {
                  Object data = (Object) libJTable.getValueAt(desiredRow, 8);
                  songLibrary.removeSongFromPlaylist(x, data);
               }//end for
               Update_table();
            }//end if
         }//end if
      }//end if
   }

   // Gets the container
   private JPanel getCardContainerPanel() {
      return container;
   }

   // Create and shows the UI
   private static void createAndShowUI() {
      try {
         Main_GUI mainGUI = new Main_GUI();
         JFrame frame = new JFrame("MP3 Player");
         //get data from database
         frame.setSize(1000, 800);
         frame.setResizable(false);
         frame.setBackground(Color.white);

         if (!loadTableData) 
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         else 
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

         frame.setLayout(new BorderLayout());
         frame.setJMenuBar(mainGUI.createMenuBar());
         frame.getContentPane().add(mainGUI.getCardContainerPanel(),
          BorderLayout.SOUTH);
         frame.pack();
         frame.setVisible(true);
      } catch (Exception ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }

   }

   private void Update_table() {
      try {
         if (!loadTableData) {
            TableModel libraryModel = songLibrary.getLibraryData();
            if (libraryModel != null)
               libJTable.setModel(libraryModel);
            //playlistLibraryJTree.setSelectionPath(new TreePath(libraryNode.
            // getPath())); 
         } else {
            TreePath path = playlistLibraryJTree.getSelectionPath();
            if (path != null) {
               //DefaultTreeModel model = (DefaultTreeModel) 
               // (playlistLibraryJTree.getModel());
               DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) 
                (path.getLastPathComponent());
               String stringForm = treeNode.toString();
               int x = songLibrary.getPlaylist(stringForm);
               ArrayList<String> songs = songLibrary.getPlaylistSongs(x);
               String query = "";
               for (String s : songs) {
                  query += "SELECT * FROM LIBRARY WHERE PKEY = ";
                  query += s + " UNION ALL ";
               }//end for
               if (query.length() > 12) 
                  query = query.substring(0, query.length() - 11);
               //Add the order by clause to queries that have been created
               if (!query.equals(""))
                  query += " ORDER BY SONG ASC";
               TableModel playlistModel = songLibrary.getPlaylistData(
                query);
               if (playlistModel != null)
                  libJTable.setModel(playlistModel);
            }//end if
         }//end else

         //highlight playing song if it is located in table
         if (player != null) {
            int totalRows = libJTable.getRowCount();
            for (int row = 0; row < totalRows; row++) {
               Object data = (Object) libJTable.getValueAt(row, 0);
               if (currentSongPlaying != null && currentSongPlaying.equals(
                (String) data)) {
                  libJTable.setRowSelectionInterval(row, row);
                  libJTable.scrollRectToVisible(new Rectangle(
                   libJTable.getCellRect(row, row, true)));
                  break;
               }//end if
            }//end for
         }

         libJTable.getColumnModel().getColumn(0).setMinWidth(0);
         libJTable.getColumnModel().getColumn(0).setMaxWidth(0);
         libJTable.getColumnModel().getColumn(1).setMinWidth(0);
         libJTable.getColumnModel().getColumn(1).setMaxWidth(0);
         libJTable.getColumnModel().getColumn(8).setMinWidth(0);
         libJTable.getColumnModel().getColumn(8).setMaxWidth(0);

         libJTable.getColumnModel().getColumn(2).setMinWidth(115);

         //toggle album column when appropriate
         if (albumColumnIsChecked) {
            libJTable.getColumnModel().getColumn(4).setMinWidth(115);
         } else {
            libJTable.getColumnModel().getColumn(4).setMinWidth(0);
            libJTable.getColumnModel().getColumn(4).setMaxWidth(0);
         }

         //toggle artist column when appropriate
         if (artistColumnIsChecked) {
            libJTable.getColumnModel().getColumn(3).setMinWidth(115);
         } else {
            libJTable.getColumnModel().getColumn(3).setMinWidth(0);
            libJTable.getColumnModel().getColumn(3).setMaxWidth(0);
         }

         //toggle years column when appropriate
         if (yearColumnIsChecked) {
            libJTable.getColumnModel().getColumn(5).setMinWidth(115);
         } else {
            libJTable.getColumnModel().getColumn(5).setMinWidth(0);
            libJTable.getColumnModel().getColumn(5).setMaxWidth(0);
         }

         //toggle genre column when appropriate
         if (genreColumnIsChecked) {
            libJTable.getColumnModel().getColumn(6).setMinWidth(115);
         } else {
            libJTable.getColumnModel().getColumn(6).setMinWidth(0);
            libJTable.getColumnModel().getColumn(6).setMaxWidth(0);
         }

         //toggle comments comments column when appropriate
         if (commentColumnIsChecked) {
            libJTable.getColumnModel().getColumn(7).setMinWidth(115);
         } else {
            libJTable.getColumnModel().getColumn(7).setMinWidth(0);
            libJTable.getColumnModel().getColumn(7).setMaxWidth(0);
         }

      } catch (Exception e) {
         JOptionPane.showMessageDialog(null, e);
      }
   }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">
   private void initComponents() {

      libJScrollPane = new javax.swing.JScrollPane();
      libJTable = new javax.swing.JTable() {
         @Override
         public Component prepareRenderer(TableCellRenderer renderer, int row, 
          int column) {
            Component component = super.prepareRenderer(renderer, row, column);
            int rendererWidth = component.getPreferredSize().width;
            TableColumn tableColumn = getColumnModel().getColumn(column);
            tableColumn.setPreferredWidth(Math.max(rendererWidth
             + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
            return component;
         }

         @Override
         public boolean isCellEditable(int row, int column) {
            //all cells false
            return false;
         }
      };
      libJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      final KeyListener keyList = new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent evt) {

            if (evt.getKeyCode() == KeyEvent.VK_RIGHT && evt.isControlDown()) {
               forwardJButtonActionPerformed();
            }

            if (evt.getKeyCode() == KeyEvent.VK_LEFT && evt.isControlDown()) {
               backwardJButtonActionPerformed();
            }

            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
               playJButtonActionPerformed();
            }
         }
      };

      libJTable.addKeyListener(keyList);

      //Initialize which columns are displayed based on the previous session.
      albumColumn = new JCheckBoxMenuItem("Album");
      artistColumn = new JCheckBoxMenuItem("Artist");
      yearColumn = new JCheckBoxMenuItem("Year");
      genreColumn = new JCheckBoxMenuItem("Genre");
      commentColumn = new JCheckBoxMenuItem("Comment");

      try {
         ArrayList<String> columnValues
          = songLibrary.updateDisplayedColumnValues(albumColumnIsChecked,
           artistColumnIsChecked, yearColumnIsChecked, genreColumnIsChecked,
           commentColumnIsChecked);

         albumColumnIsChecked = Boolean.parseBoolean(columnValues.get(0));
         albumColumn.setSelected(albumColumnIsChecked);

         artistColumnIsChecked = Boolean.parseBoolean(columnValues.get(1));
         artistColumn.setSelected(artistColumnIsChecked);

         yearColumnIsChecked = Boolean.parseBoolean(columnValues.get(2));
         yearColumn.setSelected(yearColumnIsChecked);

         genreColumnIsChecked = Boolean.parseBoolean(columnValues.get(3));
         genreColumn.setSelected(genreColumnIsChecked);

         commentColumnIsChecked = Boolean.parseBoolean(columnValues.get(4));
         commentColumn.setSelected(commentColumnIsChecked);

      } catch (Exception ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }

      headerPopUpMenu = new JPopupMenu();
      popUpMenu = new JPopupMenu();
      jTreePopUpMenu = new JPopupMenu();

      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

      //create the root node
      baseNode = new DefaultMutableTreeNode("Base Node");
      //create the child nodes
      libraryNode = new DefaultMutableTreeNode("Library");
      playlistNode = new DefaultMutableTreeNode("Playlist");

      try {
         //add existing playlists into the playlistNode
         songLibrary.populatePlaylistNames(playlistNode);
      } catch (Exception ex) {
         Logger.getLogger(Main_GUI.class.getName()).log(Level.SEVERE, null,
          ex);
      }
      //add the child nodes to the root node
      baseNode.add(libraryNode);
      baseNode.add(playlistNode);

      //create the tree by passing in the root node
      playlistLibraryJTree = new JTree(baseNode);
      playlistLibraryJTree.setRootVisible(false);
      add(playlistLibraryJTree);

      libJTable.setModel(new DefaultTableModel(libraryData, header));
      libJScrollPane.setViewportView(libJTable);

      GroupLayout layout = new GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
       layout.createParallelGroup(GroupLayout.Alignment.LEADING)
       .addGroup(layout.createSequentialGroup()
        .addComponent(libJScrollPane, GroupLayout.PREFERRED_SIZE, 1000,
         GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, Short.MAX_VALUE)
       )
      );
      layout.setVerticalGroup(
       layout.createParallelGroup(GroupLayout.Alignment.LEADING)
       .addGroup(layout.createSequentialGroup()
        .addComponent(libJScrollPane, GroupLayout.DEFAULT_SIZE, 289,
         Short.MAX_VALUE)
        .addContainerGap()
       )
      );
      Update_table();
      pack();
   }// </editor-fold>//GEN-END:initComponents

// Threadsafe runnable
   /**
    * @param args the command line arguments
    * @throws com.mpatric.mp3agic.UnsupportedTagException
    * @throws com.mpatric.mp3agic.InvalidDataException
    * @throws java.io.IOException
    * @throws com.mpatric.mp3agic.NotSupportedException
    */
   public static void main(String[] args) throws UnsupportedTagException,
    InvalidDataException, IOException,
    NotSupportedException {
      /* Create and display the form */
        //Library songLibrary1 = new Library();
      //songLibrary1.createTable();
      //songLibrary1.deleteLibraryTable();
      //songLibrary1.deletePlaylistTable();
      java.awt.EventQueue.invokeLater(() -> {
         try {
            createAndShowUI();
         }//end try
         catch (Exception e) {
         }//end catch
      });
   }
} 