
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.mpatric.mp3agic.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

public class Library {

   private final MP3_Database myDatabase = new MP3_Database();
   private final Connection con = myDatabase.connectToDatabase();
   private Vector<Vector<String>> data;//contains data from database

   //adds desired mp3 song info to the LIBRARY table (which is used to 
   //represent the user's Song Library for this project)
   public void addSongToLibrary(Mp3File Song) {
      myDatabase.addLibraryRow(con, Song);
   }//close addSongToLibrary(...)

   //adds name of the newly created playlist to the PLAYLISTS_NAMES table
   public void addNewPlaylist(String Name) {
      //only add name to the table if it is not null
      if (Name != null)
         myDatabase.addPlaylistNameRow(con, Name);
   }//close addNewPlaylist(...)

   //adds the key to the mp3 song to the PLAYLIST_SONGS table (to prevent
   //having to add all of the song info to the table)
   public void addSongToPlaylist(int playlist, Object songKey) {
      myDatabase.addPlaylistSongRow(con, playlist, songKey);
   }//close addSongToPlaylist(...)

   //Displays the appropriate fields in the window if they are selected by the
   //user. By default the Album and Artist fields are displayed along with the
   //title of the song. This info is kept in the COLUMN_DISPLAY_TABLE table
   public void updateDisplayedColumns(boolean Album, boolean Artist, boolean 
    Year, boolean Genre, boolean Comments) {
      myDatabase.updateColumnDisplayTable(con, Album, Artist, Year, Genre,
       Comments);
   }//close updateDisplayedColumns(...)

   //Adds recently played song names to the RECENTLY_PLAYED table, which keeps 
   //track of recently played songs.
   public void updateRecentlyPlayedSongs(String fileLoc, String songName) {
      myDatabase.addRecentlyPlayedTableRow(con, fileLoc, songName);
   }//close updateRecentlyPlayedSongs(...)

   //returns the size of the RECENTLY_PLAYED table
   //e.g. how many songs have been recently played (we only display the 10
   //most recently played songs)
   public int getRecentlyPlayedSongsNumber() {
      return myDatabase.getRecentlyPlayedTableSize(con);
   }//close getRecentlyPlayedSongsNumber()

   //Deletes the song information and its data from the LIBRARY table
   //(The User's Library)
   public void removeSongFromLibrary(Object SongKey) {
      //Only delete song data associated with a valid SongKey (meaning that  
      //it exist in the database that holds all of the Library song data)
      if (SongKey != null)
         myDatabase.deleteLibraryRow(con, SongKey);
   }//close removeSongFromLibrary(...)

   //Deletes the desired playlist from the PLAYLISTS_NAMES table
   public void removePlaylist(String Name) {
      //Only delete a playlist whose Name is not null
      if (Name != null) 
         myDatabase.deletePlaylistNamesRow(con, Name);
   }//close removePlaylist(...)

   //Deletes an entire row from the PLAYLIST_SONGS table
   //Used when deleting an entire playlist
   public void removePlaylistSongsRow(int playlistKey) {
      //Only delete row from a valid playlist
      if (playlistKey >= 1)
         myDatabase.deletePlaylistSongsRow(con, playlistKey);
   }//close removePlaylistSongsRow(...)

   //Deletes specified song infromation from desired playlist
   public void removeSongFromPlaylist(int playlistKey, Object songKey) {
      //Only delete song info if both the song/playlist Keys are not empty
      if (playlistKey >= 1 && songKey != null)
         myDatabase.deleteSongFromPlaylist(con, playlistKey, songKey);
   }//close removeSongFromPlaylist(...)

   //Deletes entire row from the COLUMN_DISPLAY_TABLE table
   public void removeColumnDisplayTableRow() {
      myDatabase.deleteColumnDisplayTableRow(con);
   }//close removeColumnDisplayTableRow()

   //Deletes entire row from the RECENTLY_PLAYED table
   public void removeSongFromRecentlyPlayed() {
      myDatabase.deleteRecentlyPlayedTableRow(con);
   }//close removeSongFromRecentlyPlayed()

   //Removes song information that was deleted by the user from any playlists
   //that they were previously in
   public void removeDeletedSongsFromPlaylists(Object songKey) {
      if (songKey != null)
         myDatabase.deleteDeletedSongsFromPlaylists(con, songKey);
   }//close removeDeletedSongsFromPlaylists(...)

   //Creates all of the associated tables for the database
   //This code will only be called once when creating all of the tables
   public void createTable() {
      //myDatabase.createLibraryTable(con);
      myDatabase.createPlaylistTable(con);
      //myDatabase.createColumnDisplayTable(con);
      //myDatabase.createRecentlyPlayedTable(con);
      //myDatabase.createPlaylistSongsTable(con); 
   }//close createTable()

   //Deletes the entire LIBRARY table
   public void deleteLibraryTable() {
      myDatabase.dropLibraryTable(con);
   }//clsoe deleteLibraryTable()

   //Deletes the entire PLAYLISTS_NAMES table
   public void deletePlaylistTable() {
      myDatabase.dropPlaylistTable(con);
   }//close deletePlaylistTable()

   //Deletes the entire PLAYLIST_SONGS table
   public void deletePlaylistSongsTable() {
      myDatabase.dropPlaylistSongsTable(con);
   }//close deletePlaylistSongsTable()

   //Deletes the entire COLUMN_DISPLAY_TABLE table
   public void deleteColumnDisplayTable() {
      myDatabase.dropColumnDisplayTable(con);
   }//close deleteColumnDisplayTable()

   //Deletes the entire RECENTLY_PLAYED table
   public void deleteRecentlyPlayedTable() {
      myDatabase.dropRecentlyPlayedTable(con);
   }//close deleteRecentlyPlayedTable()

   //Updates COLUMN_DISPLAY_TABLE table accordingly based on the 
   //user's inputs for what columns to show
   public ArrayList<String> updateDisplayedColumnValues(boolean Album,
    boolean Artist, boolean Year, boolean Genre, boolean Comments) {
      try {
         return myDatabase.assignColumnValues(con, Album, Artist, Year,
          Genre, Comments);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
   }//close updateDisplayedColumnValues(...)

   //Returns a list containint all of the songs in the Library (LIBRARY table)
   public Vector<Vector<String>> getSongsInLibrary() {
      try {
         data = myDatabase.getLibrarySongs(con);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return data;
   }//close getSongsInLibrary()

   //Retrieve the song key for the specified song in the library 
   //(LIBRARY table)
   public Object getLibrarySongId(String path) {
      try {
         return myDatabase.getLibrarySongKey(con, path);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return null;
   }//close getLibrarySongId(...);

   //Retrieve the recently played song path in the RECENTLY_PLAYED table
   public Object getPlayRecentSong(String songName) {
      try {
         return myDatabase.getPlayRecentSongPath(con, songName);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return null;
   }//close getPlayRecentSong(...)

   //Returns true/false based on whether the provided Playlist name is located
   //within the PLAYLISTS_NAMES table or not.
   public boolean getPlaylistNameAvailability(String playlistName) {
      boolean result = false;
      try {
         result = myDatabase.comparePlaylistNames(con, playlistName);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return result;
   }//close getPlaylistNameAvailability(...)   

   //Initializes the Playlist Tree (shows the Playlist names of the 
   //playlists that the user has created)
   public void populatePlaylistNames(DefaultMutableTreeNode node) {
      try {
         myDatabase.initializePlaylistTree(con, node);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
   }//close populatePlaylistNames(...)

   //Returns the Playlist key associated with the Playlist name input.
   public int getPlaylist(String name) {
      try {
         return myDatabase.getPlaylistKey(con, name);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return -1;
   }//close getPlaylist(...)

   //Returns a list of the different song names located within a Playlist
   //based on the playlistKey input
   public ArrayList<String> getPlaylistSongs(int playlistKey) {
      ArrayList<String> values = null;
      try {
         values = myDatabase.getPlaylistSongNames(con, playlistKey);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return values;
   }//close getPlaylistSongs(...)  

   //Returns a list of the names of the most recently played songs located 
   //within the RECENTLY_PLAYED table
   public ArrayList<String> getRecentlyPlayedSongs() {
      ArrayList<String> values = null;
      try {
         values = myDatabase.getRecentlyPlayedSongNames(con);
      } catch (Exception ex) {
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
      }//end catch
      return values;
   }//close getRecentlyPlayedSongs()

   //Returns true/false based on whether a particular song exists within the
   //Library (LIBRARY table) or not
   public boolean checkIfSongExistsInLibrary(String songPath) {
      return myDatabase.compareLibrarySongs(con, songPath);
   }//close checkIfSongExistsInLibrary(...)

   //Retrieves all of the rows (which contain song information) in the
   //Library (LIBRARY table)
   public TableModel updateLibraryData() {
      TableModel result = null;
      if (con != null) {
         try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM"
          + " LIBRARY ORDER BY SONG ASC"); ResultSet rs = stmt.executeQuery()){
            result = convertResultSetToTableModel(rs);
         } catch (SQLException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
             null, ex);
         }//end catch
      }//end if
      return result;
   }//close updateLibraryData()

   //Retrieves all of the rows (which contain song information) in the
   //PLAYLIST_SONGS table
   public TableModel updatePlaylistData(String query) {
      TableModel result = null;
      if (con != null) {
         //if the query input is empty, that means that we do not have a
         //Playlist to find, so we change the query to do nothing.
         if (query.equals("")) 
            query = "SELECT * FROM LIBRARY WHERE 1=0 ";
         try (PreparedStatement stmt = con.prepareStatement(query);
          ResultSet rs = stmt.executeQuery()) {
            result = convertResultSetToTableModel(rs);
         } catch (SQLException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
             null, ex);
         }//end catch
      }//end if
      return result;
   }//close updatePlaylistData(...) 

   public TableModel convertResultSetToTableModel(ResultSet rs) {
      try {
         //Retrieve MetaData form the ResultSet
         ResultSetMetaData metaData = rs.getMetaData();
         int numberOfColumns = metaData.getColumnCount();
         Vector columnNames = new Vector();

         // Get the column names
         for (int column = 0; column < numberOfColumns; column++)
            columnNames.addElement(metaData.getColumnLabel(column + 1));

         // Get all rows.
         Vector rows = new Vector();
         while (rs.next()) {
            Vector newRow = new Vector();
            //add all of the appropriate data to the newly created row
            for (int i = 1; i <= numberOfColumns; i++)
               newRow.addElement(rs.getObject(i));
            //add the ceated row to the collection of rows
            rows.addElement(newRow);
         }//end while
         //Return the TableModel
         return new DefaultTableModel(rows, columnNames);
      } catch (Exception e) {
         //log the exception and return a null value (to indicate a failure)
         Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, e);
      }//end catch
      return null;
   }//close convertResultSetToTableModel(ResultSet rs) 

   public final class MP3_Database {

      //Connects to the database containing all of our data for the project
      public Connection connectToDatabase() {
         try {
            String host = "jdbc:derby://localhost:1527/MP3_Player_Database";
            String uName = "MP3_Player_Database";
            String uPass = "MP3_Player_Database";
            Connection con = DriverManager.getConnection(host, uName,
             uPass);
            return con;
         } catch (SQLException err) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
             null, err);
            return null;
         }//end catch 
      }//close connectToDatabase()   

      //Creates the Library table
      public void createLibraryTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("CREATE TABLE LIBRARY (FILE VARCHAR(200),"
                + " TRACK VARCHAR(20), SONG VARCHAR(100),"
                + " ARTIST VARCHAR(60), ALBUM VARCHAR(100),"
                + " RELEASE_YEAR VARCHAR(20), GENRE VARCHAR(100),"
                + " COMMENTS VARCHAR(500),PKey int primary key"
                + " generated always as identity)");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
      }//close createLibraryTable(...)

      //Creates the PLAYLISTS_NAMES table
      public void createPlaylistTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("CREATE TABLE PLAYLISTS_NAMES (PKey int"
                + " primary key generated always as identity,"
                + " PLAYLIST_NAME VARCHAR(500))");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
      }//close createPlaylistTable

      //Creates the PLAYLIST_SONGS table
      public void createPlaylistSongsTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("CREATE TABLE PLAYLIST_SONGS (PLAYLIST_KEYS"
                + " VARCHAR(200), SONG_KEYS VARCHAR(200))");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
      }//close createPlaylistSongsTable(...)  

      //Creates the COLUMN_DISPLAY_TABLE table
      public void createColumnDisplayTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("CREATE TABLE COLUMN_DISPLAY_TABLE"
                + " (ALBUM_DISPLAY BOOLEAN, ARTIST_DISPLAY BOOLEAN,"
                + " YEAR_DISPLAY BOOLEAN, GENRE_DISPLAY BOOLEAN,"
                + " COMMENT_DISPLAY BOOLEAN)");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
      }//close createColumnDisplayTable(...)

      //Creates the RECENTLY_PLAYED table
      public void createRecentlyPlayedTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("CREATE TABLE RECENTLY_PLAYED (SONG_NUM int"
                + " primary key generated always as identity,"
                + " FILE_LOCATION VARCHAR(100), SONG_NAME VARCHAR(100))");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
      }//close createRecentlyPlayedTable(...)

      //Deletes the LIBRARY table
      public void dropLibraryTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("DROP TABLE LIBRARY");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch  
         }//end if
      }//close dropLibraryTable(...)

      //Deletes the PLAYLISTS_NAMES table
      public void dropPlaylistTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("DROP TABLE PLAYLISTS_NAMES");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch  
         }//end if
      }//close dropPlaylistTable(...)      

      //Deletes the PLAYLIST_SONGS table
      public void dropPlaylistSongsTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("DROP TABLE PLAYLIST_SONGS");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch  
         }//end if
      }//close dropPlaylistSongsTable(...)  

      //Deletes the COLUMN_DISPLAY_TABLE table
      public void dropColumnDisplayTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("DROP TABLE COLUMN_DISPLAY_TABLE");
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch  
         }//end if
      }//close dropColumnDisplayTable(...)

      //Deletes the RECENTLY_PLAYED table
      public void dropRecentlyPlayedTable(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute("DROP TABLE RECENTLY_PLAYED");
            } catch (SQLException err) {
               System.out.println(err.getMessage());
            }//end catch  
         }//end if
      }//close dropRecentlyPlayedTable(...)

      //Adds the song information into the Library (LIBRARY table)
      public void addLibraryRow(Connection DatabaseConnection, Mp3File Song) {
         //make sure that the input Song has ID3v1 or ID3v2 tags
         if (DatabaseConnection != null && (Song.hasId3v1Tag()
          || Song.hasId3v2Tag())) {
            String query = "INSERT INTO LIBRARY (FILE, TRACK, SONG,"
             + " ARTIST, ALBUM, RELEASE_YEAR, GENRE, COMMENTS) VALUES ";

            //Add File Location to query
            query += Song.getFilename() != null ? "('" + Song.getFilename()
             + "'," : "('Unknown',";

            if (Song.hasId3v1Tag()) {
               //Add in ID3v1 Tags if the song has any
               ID3v1 id3v1Tag = Song.getId3v1Tag();
               //Add Track Number to query             
               query += id3v1Tag.getTrack() != null ? "'"
                + id3v1Tag.getTrack() + "', " : "'Unknown', ";
               //Add Song Title to query
               query += id3v1Tag.getTitle() != null ? "'"
                + id3v1Tag.getTitle() + "', " : "'Unknown', ";
               //Add Artist to query
               query += id3v1Tag.getArtist() != null ? "'"
                + id3v1Tag.getArtist() + "', " : "'Unknown', ";
               //Add Album Name to query
               query += id3v1Tag.getAlbum() != null ? "'"
                + id3v1Tag.getAlbum() + "', " : "'Unknown', ";
               //Add Release Year to query
               query += id3v1Tag.getYear() != null ? "'"
                + id3v1Tag.getYear() + "', " : "'Unknown', ";
               //Add Genre Description to query
               query += id3v1Tag.getGenreDescription() != null ? "'"
                + id3v1Tag.getGenreDescription() + "', " : "Unknown";
               //Add Comments to query
               query += id3v1Tag.getComment() != null ? "'"
                + id3v1Tag.getComment() + "')" : "'No Comments')";
            } else {
               //Add in ID3v2 Tags
               ID3v2 id3v2Tag = Song.getId3v2Tag();
               //Add Track Number to query             
               query += id3v2Tag.getTrack() != null ? "'"
                + id3v2Tag.getTrack() + "', " : "'Unknown', ";
               //Add Song Title to query
               query += id3v2Tag.getTitle() != null ? "'"
                + id3v2Tag.getTitle() + "', " : "'Unknown', ";
               //Add Artist to query
               query += id3v2Tag.getArtist() != null ? "'"
                + id3v2Tag.getArtist() + "', " : "'Unknown', ";
               //Add Album Name to query
               query += id3v2Tag.getAlbum() != null ? "'"
                + id3v2Tag.getAlbum() + "', " : "'Unknown', ";
               //Add Release Year to query
               query += id3v2Tag.getYear() != null ? "'"
                + id3v2Tag.getYear() + "', " : "'Unknown', ";
               //Add Genre Description to query
               query += id3v2Tag.getGenreDescription() != null ? "'"
                + id3v2Tag.getGenreDescription() + "', " : "Unknown";
               //Add Comments to query
               query += id3v2Tag.getComment() != null ? "'"
                + id3v2Tag.getComment() + "')" : "'No Comments')";
            }//end else
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch     
         }//end if    
      }//close addLibraryRow(...)

      //Adds the newly created Playlist info into the PLAYLISTS_NAMES
      public void addPlaylistNameRow(Connection DatabaseConnection,
       String Name) {
         if (DatabaseConnection != null) {
            String query = "INSERT INTO PLAYLISTS_NAMES"
             + " (PLAYLIST_NAME) VALUES ('" + Name + "')";
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch     
         }//end if    
      }//close addPlaylistNameRow(...)       

      //Associates the desired songKey with the appropriate playlistKey in
      //the PLAYLIST_SONGS table
      public void addPlaylistSongRow(Connection DatabaseConnection, int 
       playlistKey, Object songKey) {
         if (DatabaseConnection != null) {
            String query = "INSERT INTO PLAYLIST_SONGS VALUES ('"
             + playlistKey + "', '" + songKey + "')";
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch     
         }//end if    
      }//close addPlaylistSongRow(...)  

      //Adds the appropriate values of what columns should be displayed
      //to the user. The COLUMN_DISPLAY_TABLE table is used to achieve this.
      public void updateColumnDisplayTable(Connection DatabaseConnection,
       boolean Album, boolean Artist, boolean Year, boolean Genre,
       boolean Comments) {
         if (DatabaseConnection != null) {
            String query = "INSERT INTO COLUMN_DISPLAY_TABLE VALUES ('"
             + Album + "', '" + Artist + "', '" + Year + "', '" + Genre
             + "', '" + Comments + "')";
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch     
         }//end if    
      }//close updateColumnDisplayTable(...)

      //Adds a recently played song into the RECENTLY_PLAYED table
      public void addRecentlyPlayedTableRow(Connection DatabaseConnection,
       String location, String songName) {
         if (DatabaseConnection != null) {
            String query = "INSERT INTO RECENTLY_PLAYED (FILE_LOCATION, "
             + "SONG_NAME) VALUES ('" + location + "', '" + songName + "')";
            try (Statement stmt = DatabaseConnection.createStatement()) {
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
      }//close addRecentlyPlayedTableRow(...)

      //Returns the size of the RECENTLY_PLAYED table
      public int getRecentlyPlayedTableSize(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            String query = "SELECT COUNT(*) FROM RECENTLY_PLAYED";
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery();) {
               //return count value from query (if it has any data)
               if (rs.next())
                  return rs.getInt(1);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch
         }//end if
         return 0;
      }//close getRecentlyPlayedTableSize(...)

      //Deletes a specified row from the Library (LIBRARY table)
      public void deleteLibraryRow(Connection DatabaseConnection, Object key) {
         if (DatabaseConnection != null && key != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM LIBRARY WHERE";
               query += " PKEY = " + key + "";
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch          
         }//end if      
      }//close deleteLibraryRow(...)

      //Deletes a specified row from the PLAYLISTS_NAMES table
      public void deletePlaylistNamesRow(Connection DatabaseConnection,
       String playlistName) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM PLAYLISTS_NAMES WHERE";
               query += " PLAYLIST_NAME = '" + playlistName + "'";
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch          
         }//end if      
      }//close deletePlaylistNamesRow(...)

      //Deletes a specified row from the PLAYLIST_SONGS table
      public void deletePlaylistSongsRow(Connection DatabaseConnection, int 
       playlistKey) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM PLAYLIST_SONGS WHERE";
               query += " PLAYLIST_KEYS = '" + playlistKey + "'";
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch          
         }//end if      
      }//close deletePlaylistSongsRow(...)      

      //Deletes a specified row from the PLAYLIST_SONGS table (by removing
      //the association between a song and the corresponding playlist)
      public void deleteSongFromPlaylist(Connection DatabaseConnection, int 
       playlistKey, Object songKey) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM PLAYLIST_SONGS WHERE";
               query += " PLAYLIST_KEYS = '" + playlistKey + "' AND "
                + "SONG_KEYS = '" + songKey + "'";
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch          
         }//end if                
      }//close DeleteSongsFromPlaylist(...)

      //Delete row from the COLUMN_DISPLAY_TABLE.
      public void deleteColumnDisplayTableRow(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM COLUMN_DISPLAY_TABLE WHERE 1=1";
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch          
         }//end if      
      }//close deleteColumnDisplayTableRow(...)

      //Deletes row from the RECENTLY_PLAYED table (this table only has 10
      //rows on it at any given time. As new songs are played, the oldest
      //song is removed and the newer songs are added in as needed.
      public void deleteRecentlyPlayedTableRow(Connection DatabaseConnection) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM RECENTLY_PLAYED"
                + " WHERE SONG_NUM = (SELECT MIN(SONG_NUM) FROM"
                + " RECENTLY_PLAYED)";
               stmt.execute(query);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch          
         }//end if      
      }//close deleteRecentlyPlayedTableRow(...)

      //Removes any songs that the user deleted from the library from
      //any playlists that they were associated with.
      public void deleteDeletedSongsFromPlaylists(Connection DatabaseConnection, 
       Object songKey) {
         if (DatabaseConnection != null) {
            try (Statement stmt = DatabaseConnection.createStatement()) {
               String query = "DELETE FROM PLAYLIST_SONGS WHERE";
               query += " SONG_KEYS = '" + songKey + "'";
               stmt.execute(query);
            } catch (SQLException err) {
               System.out.println(err.getMessage());
            }//end catch          
         }//end if                 
      }//close deleteDeletedSongsFromPlaylists(...)

      //Returns a list of the songs in the Library (LIBRARY table)
      public Vector<Vector<String>> getLibrarySongs(Connection 
       DatabaseConnection) {
         Vector<Vector<String>> songVector = new Vector<>();
         if (DatabaseConnection != null) {
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement("select * from LIBRARY");
             ResultSet rs = statement.executeQuery()) {
               while (rs.next()) {
                  Vector<String> Song = new Vector<>();
                  Song.add(rs.getString(1));//File_Location
                  Song.add(rs.getString(2));//Track_Number
                  Song.add(rs.getString(3));//Song_Title
                  Song.add(rs.getString(4));//Artist
                  Song.add(rs.getString(5));//Album
                  Song.add(rs.getString(6));//Release_Year
                  Song.add(rs.getString(7));//Genre
                  Song.add(rs.getString(8));//Comments
                  Song.add(rs.getString(9));//PKeys
                  songVector.add(Song);
               }//end while
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch    
         }//end if
         return songVector;
      }//close getLibrarySongs(...)

      //Checks to see if the current song exists in the user's Library
      //(LIBRARY table)
      public boolean compareLibrarySongs(Connection DatabaseConnection,
       String fileName) {
         boolean exists = false;
         if (DatabaseConnection != null) {
            String query = "select * from LIBRARY";
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
               while (rs.next()) {
                  if (fileName.equals(rs.getString(1))) {
                     exists = true; //song is in the library
                     break;
                  }//end if
               }//end while
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch   
         }//end if
         return exists;
      }//close compareLibrarySongs(...)

      //Returns the SongKey of the provided song path (if its in the Library)
      public Object getLibrarySongKey(Connection DatabaseConnection, String 
       path) {
         Object returnValue = null;
         if (DatabaseConnection != null) {
            String query = "select * from LIBRARY WHERE ";
            query += " FILE = '" + path + "'";
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
               //set the output to the first entry in the result of the 
               //query (if the result has any data)
               if (rs.next())
                  returnValue = rs.getString(9);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch    
         }//end if
         return returnValue;
      }//close getLibrarySongKey(...)

      //Returns the path of a recently played song
      public Object getPlayRecentSongPath(Connection DatabaseConnection,
       String songName) {
         Object returnValue = null;
         if (DatabaseConnection != null) {
            String query = "select * from RECENTLY_PLAYED WHERE ";
            query += " SONG_NAME = '" + songName + "'";
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery();) {
               //set the output to the first entry in the result of the
               //query (if the result has any data)
               if (rs.next())
                  returnValue = rs.getString(2);
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch   
         }//end if
         return returnValue;
      }//close getPlayRecentSongPath(...)

      //Returns true/false based on whether a Playlist name is available
      //e.g. it is not already being used
      public boolean comparePlaylistNames(Connection DatabaseConnection,
       String proposedPlaylistName) {
         if (DatabaseConnection != null) {
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(
             "select * from PLAYLISTS_NAMES");
             ResultSet rs = statement.executeQuery()) {
               while (rs.next()) {
                  //return false if the desired name is already used
                  if (proposedPlaylistName != null
                   && proposedPlaylistName.equals(rs.getString(2))) 
                     return false;

                  if (proposedPlaylistName != null
                   && (proposedPlaylistName.equals("Library")
                   || proposedPlaylistName.equals("Playlists"))) 
                     return false;
                }//end while
             } catch (SQLException err) {
                Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                 null, err);
             }//end catch   
         }//end if
         return true; //Playlist name is available; return true
      }//close comparePlaylistNames(...)   

      //Initializes the Playlist Tree (Shows the user the playlists that
      //they have created)
      public void initializePlaylistTree(Connection DatabaseConnection,
       DefaultMutableTreeNode node) {
         if (DatabaseConnection != null) {
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(
             "select * from PLAYLISTS_NAMES");
             ResultSet rs = statement.executeQuery()) {
               //Add Playlist Name to the Playlist Tree
               while (rs.next()) 
                  node.add(new DefaultMutableTreeNode(rs.getString(2)));
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch  
         }//end if
      }//close initializePlaylistTree(...)

      //Returns the playlistKey associated with the provided playlistName.
      public int getPlaylistKey(Connection DatabaseConnection,
       String playlistName) {
         int returnValue = -1;
         if (DatabaseConnection != null) {
            String query = "select * from PLAYLISTS_NAMES WHERE";
            query += " PLAYLIST_NAME = '" + playlistName + "'";

            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
               //Update returnValue if query result has any data
               if (rs.next())
                  returnValue = Integer.parseInt(rs.getString(1));
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch   
         }//end if
         return returnValue;
      }//close getPlaylistKey(...)

      //Returns a list of songs associated with the provided playlist key
      public ArrayList<String> getPlaylistSongNames(Connection 
       DatabaseConnection, int key) {
         ArrayList<String> songList = new ArrayList<>();
         if (DatabaseConnection != null) {
            String query = "select * from PLAYLIST_SONGS WHERE"
             + " PLAYLIST_KEYS = ";
            query += "'" + key + "'";

            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
               //Add songs names to ArrayList
               while (rs.next())
                  songList.add(rs.getString(2)); //Song Name
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch   
         }//end if
         return songList;
      }//close getPlaylistSongNames(...)     

      //Returns the song information located within the RECENTLY_PLAYED table
      public ArrayList<String> getRecentlyPlayedSongNames(Connection 
       DatabaseConnection) {
         ArrayList<String> songList = new ArrayList<>();
         if (DatabaseConnection != null) {
            String query = "select * from RECENTLY_PLAYED";
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
               //Add song names to ArrayList
               while (rs.next())
                  songList.add(rs.getString(3));//Song Name
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch   
         }//end if
         return songList;
      }//close getRecentlyPlayedSongNames(...)

      //Returns an ArrayList containing the designated ColumnValues selected
      //by the user.
      public ArrayList<String> assignColumnValues(Connection DatabaseConnection, 
       boolean Album, boolean Artist, boolean Year, boolean Genre, boolean 
       Comments) throws Exception {
         ArrayList<String> columnValues = new ArrayList<>();
         if (DatabaseConnection != null) {
            String query = "select * from COLUMN_DISPLAY_TABLE";
            try (PreparedStatement statement
             = DatabaseConnection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
               //Add values to ArrayList
               while (rs.next()) {
                  columnValues.add(rs.getString(1)); //Album
                  columnValues.add(rs.getString(2)); //Artist
                  columnValues.add(rs.getString(3)); //Year
                  columnValues.add(rs.getString(4)); //Genre
                  columnValues.add(rs.getString(5)); //Comments
               }//end while
            } catch (SQLException err) {
               Logger.getLogger(Library.class.getName()).log(Level.SEVERE,
                null, err);
            }//end catch  
         }//end if
         return columnValues;
      }//close assignColumnValues(...)

   }//close MP3_Database class

}//close Library class
