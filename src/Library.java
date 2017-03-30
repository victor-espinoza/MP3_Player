import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.mpatric.mp3agic.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javazoom.jl.player.Player;

public class Library {
    private final MP3_Database myDatabase = new MP3_Database();
    private final Connection con = myDatabase.ConnectToDatabase();
    private Vector<Vector<String>> data;//contains data from database

    public void addToDatabase(Mp3File Song) {
        if (Song.hasId3v1Tag()){
            myDatabase.AddLibraryRow(con, Song);
        }
    }//close addToDatabase
    
    public void addToPlaylist(String Name){
        if (Name !=null)
            myDatabase.AddPlaylistRow(con, Name);
    }
    
    public void addToPlaylistSongs(int playlist, Object songKey){
        myDatabase.AddPlaylistSongRow(con,playlist,songKey);
    }
    
    public void addToColumnDisplayTable(boolean Album, boolean Artist, boolean 
     Year, boolean Genre, boolean Comments){
        myDatabase.AddColumnDisplayTableRow(con, Album, Artist, Year, Genre, 
         Comments);
    }
    
    public void addToRecentlyPlayedTable(String fileLoc, String songName){
        myDatabase.AddRecentlyPlayedTableRow(con, fileLoc, songName);
    }
    
    public int checkRecentlyPlayedTableSize(){
        return myDatabase.CheckRecentlyPlayedTableSize(con);
    }
    
    public void deleteFromDatabase(Object SongKey) {
        if (SongKey != null)
            myDatabase.DeleteLibraryRow(con, SongKey);
    }//close deleteFromDatabase
    
    public void deleteFromPlaylist(String Name) {
        if (Name !=null)
            myDatabase.DeletePlaylistRow(con, Name);
    }//close deleteFromDatabase
    
    public void deleteFromPlaylistSongs(int playlistKey) {
        if (playlistKey >=1)
            myDatabase.DeletePlaylistSongsRow(con, playlistKey);
    }//close deleteFromDatabase
    
    public void deleteSongsFromPlaylistSongs(int playlistKey, Object songKey) {
        if (playlistKey >=1 && songKey != null)
            myDatabase.DeleteSongsFromPlaylist(con, playlistKey,songKey);
    }//close deleteFromDatabase  
    
    public void deleteColumnDisplayTableRow(){
        myDatabase.DeleteColumnDisplayTableRow(con);
    }
    
    public void deleteFromRecentlyPlayed(){
        myDatabase.DeleteRecentlyPlayedTableRow(con);
    }
    
    public void removeDeletedSongsFromPlaylists(Object songKey) {
        if(songKey != null)
            myDatabase.RemoveDeletedSongsFromPlaylists(con, songKey);
    }//close removeDeletedSongsFromPlaylists
    
    public void createTable() {
        //myDatabase.CreateLibraryTable(con);
        //myDatabase.CreatePlaylistTable(con);
        myDatabase.CreateColumnDisplayTable(con);
        myDatabase.CreateRecentlyPlayedTable(con);
        //myDatabase.CreatePlaylistSongsTable(con);
 
    }//close deleteFromDatabase
    
    public void dropLibraryTable() {
        myDatabase.DropLibraryTable(con);
    }//close deleteFromDatabase
    
    public void dropPlaylistTable() {
        myDatabase.DropPlaylistTable(con);
    }//close deleteFromDatabase
    
    public void dropPlaylistSongsTable() {
        myDatabase.DropPlaylistSongsTable(con);
    }//close deleteFromDatabase  
    
    public void dropColumnDisplayTable(){
        myDatabase.DropColumnDisplayTable(con);
    }
    
    public void dropRecentlyPlayedTable(){
        myDatabase.DropRecentlyPlayedTable(con);
    }
    
    public ArrayList<String> assignColumnValues(boolean Album, boolean Artist, boolean Year, 
     boolean Genre, boolean Comments) throws Exception{
        return myDatabase.AssignColumnValues(con, Album, Artist, Year, Genre, Comments);
    }
    
    public Vector<Vector<String>> getSongsInLibrary(){
        try {
            data=myDatabase.GetLibrarySongs(con);
        } catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return data;
    }
    
    public Object getLibrarySongKey(String path){
        try{
            return myDatabase.GetLibrarySongKey(con,path);
        }
        catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return null;
    }
    
    public Object getPlayRecentSongPath(String songName){
        try{
            return myDatabase.GetPlayRecentSongPath(con,songName);
        }
        catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return null;
    }
    
    public boolean getPlaylistNames(String playlistName)throws Exception {
        boolean result=true;
        try {
            result=myDatabase.ComparePlaylistNames(con,playlistName);
        } catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return result;
    }    
    
    public void initializePlaylistTree(DefaultMutableTreeNode node)throws Exception {
        try {
            myDatabase.InitializePlaylistTree(con,node);
        } catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
    }  
    
    public int getPlaylistKey(String name){
        try{
            return myDatabase.GetPlaylistKey(con, name);
        } catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return -1;
    }
    
    
    public ArrayList<String> getPlaylistSongs(int playlistKey){
        ArrayList<String> values = null;
        try {
            values= myDatabase.GetPlaylistSongNames(con, playlistKey);
        } catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return values;
    }     
    
    public ArrayList<String> getRecentlyPlayedSongNames(){
        ArrayList<String> values = null;
        try {
            values= myDatabase.GetRecentlyPlayedSongNames(con);
        } catch (Exception ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return values;
    }
    
    public boolean checkIfSongExistsInLibrary(String songPath)throws Exception{
        return myDatabase.CheckIfSongExistsInLibrary(con, songPath);
    }
    
    
    public ResultSet updateLibraryData(){
        PreparedStatement stmt;
        ResultSet rs=null;
        try {
            stmt = con.prepareStatement("SELECT * FROM MP3_DATA ORDER BY "
             + "SONG ASC");

            //stmt = con.prepareStatement("SELECT FILE,TRACK,SONG,ARTIST,ALBUM,"
            // + "RELEASE_YEAR,GENRE,COMMENTS FROM MP3_DATA");
            rs=stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return rs;  
    }
    
    public ResultSet updatePlaylistData(String query){  
        PreparedStatement stmt;
        ResultSet rs=null;
        try {
            //String query = "SELECT * FROM MP3_DATA WHERE PKEY in (";
            //query += condition + ")";
            if (query.equals(""))
                query = "SELECT * FROM MP3_DATA WHERE 1=0 ";
            stmt = con.prepareStatement(query);
            //stmt = con.prepareStatement("SELECT FILE,TRACK,SONG,ARTIST,ALBUM,"
            // + "RELEASE_YEAR,GENRE,COMMENTS FROM MP3_DATA");
            rs=stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, 
             null, ex);
        }
        return rs;  
    }    
    
    public final class MP3_Database {

        public Connection ConnectToDatabase(){
            try {
                String host = "jdbc:derby://localhost:1527/MP3_Player_Database";
                String uName = "MP3_Player_Database";
                String uPass= "MP3_Player_Database";
                Connection con = DriverManager.getConnection(host, uName, uPass);
                return con;
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
                return null;
            }//end catch 
        }//close ConnectToDatabase    


        public void CreateLibraryTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("CREATE TABLE MP3_DATA (FILE VARCHAR(200), "
                 + " TRACK VARCHAR(20), SONG VARCHAR(100), "
                 + "ARTIST VARCHAR(60), ALBUM VARCHAR(100),"
                 + " RELEASE_YEAR VARCHAR(20), GENRE VARCHAR(100), "
                 + "COMMENTS VARCHAR(500),PKey int primary key "
                 +   "generated always as identity)");
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch
        }//close CreateLibraryTable
        
        
        public void CreatePlaylistTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("CREATE TABLE PLAYLIST_NAMES_TABLE (PKey int primary key"
                 +   " generated always as identity, PLAYLIST_NAME VARCHAR(500)"
                 + ")");
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch
        }//close CreatePlaylistTable

        public void CreatePlaylistSongsTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("CREATE TABLE PLAYLIST_SONGS (PLAYLIST_KEYS "
                 + "VARCHAR(200), SONG_KEYS VARCHAR(200))");
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch
        }//close CreatePlaylistSongsTable    
        
        
        public void CreateColumnDisplayTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("CREATE TABLE COLUMN_DISPLAY_TABLE (ALBUM_DISPLAY "
                 + "BOOLEAN, ARTIST_DISPLAY BOOLEAN, YEAR_DISPLAY BOOLEAN, "
                 + "GENRE_DISPLAY BOOLEAN, COMMENT_DISPLAY BOOLEAN)");
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch
        }//close CreatePlaylistTable
        
        
        public void CreateRecentlyPlayedTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("CREATE TABLE RECENTLY_PLAYED (SONG_NUM int"
                 +   " primary key generated always as identity, FILE_LOCATION "
                 + "VARCHAR(100), SONG_NAME VARCHAR(100))");
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch
        }//close CreatePlaylistTable

        public void DropLibraryTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("DROP TABLE MP3_DATA");  
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch  
        }//close DropLibraryTable

        public void DropPlaylistTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("DROP TABLE PLAYLIST_NAMES_TABLE");  
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch  
        }//close DropPlaylistTable        
        
        public void DropPlaylistSongsTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("DROP TABLE PLAYLIST_SONGS");  
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch  
        }//close DropPlaylistSongsTable   
        
        public void DropColumnDisplayTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("DROP TABLE COLUMN_DISPLAY_TABLE");  
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch  
        }//close DropColumnDisplayTable
        
        public void DropRecentlyPlayedTable(Connection DatabaseConnection){
            try (Statement stmt = DatabaseConnection.createStatement()) {
                stmt.execute("DROP TABLE RECENTLY_PLAYED");  
            }//end try
            catch(SQLException err) {
                System.out.println(err.getMessage());
            }//end catch  
        }//close DropRecentlyPlayedTable

        public void AddLibraryRow(Connection DatabaseConnection, Mp3File Song){
            if (DatabaseConnection != null && Song.hasId3v1Tag()){
                ID3v1 id3v1Tag = Song.getId3v1Tag();
                String query = "INSERT INTO MP3_DATA (FILE, TRACK,SONG, ARTIST,"
                        + " ALBUM, RELEASE_YEAR, GENRE, COMMENTS) VALUES ";               
                //Add File Location to query
                query += Song.getFilename() != null ? "('" + Song.getFilename() 
                 + "'," : "('Unknown',";
                //Add Track Number to query
                query += id3v1Tag.getTrack() != null ? "'" + id3v1Tag.getTrack() 
                 + "', " : "'Unknown', ";
                //Add Song Title to query
                query += id3v1Tag.getTitle() != null ? "'" + id3v1Tag.getTitle() 
                 + "', " : "'Unknown', ";
                //Add Artist to query
                query += id3v1Tag.getArtist() != null ? "'" + id3v1Tag.getArtist() 
                 + "', " : "'Unknown', ";
                //Add Album Name to query
                query += id3v1Tag.getAlbum() != null ? "'" + id3v1Tag.getAlbum() 
                 + "', " : "'Unknown', ";
                //Add Release Year to query
                query += id3v1Tag.getYear() != null ? "'" + id3v1Tag.getYear() 
                 + "', " : "'Unknown', ";
                //Add Genre Description to query
                query += "'" + id3v1Tag.getGenreDescription() + "', ";
                //Add Comments to query
                query += id3v1Tag.getComment() != null ? "'" + id3v1Tag.getComment() 
                 + "')" : "'No Comments')";
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    stmt.execute(query);
                }//end try
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch     
            }//end if    
        }//close AddLibraryRow
        
        public void AddPlaylistRow(Connection DatabaseConnection, String Name){
            if (DatabaseConnection != null){
                String query = "INSERT INTO PLAYLIST_NAMES_TABLE (PLAYLIST_NAME) "
                 + "VALUES ('" + Name + "')";
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    stmt.execute(query);
                }//end try
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch     
            }//end if    
        }//close AddPlaylistRow        
 
        public void AddPlaylistSongRow(Connection DatabaseConnection, int
         playlistKey, Object songKey){
            if (DatabaseConnection != null){   
                String query = "INSERT INTO PLAYLIST_SONGS VALUES ('" 
                + playlistKey + "', '" + songKey + "')";
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    stmt.execute(query);
                }//end try
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch     
            }//end if    
        }//close AddPlaylistSongRow   
        
        
        public void AddColumnDisplayTableRow(Connection DatabaseConnection, 
         boolean Album, boolean Artist, boolean Year, boolean Genre, 
         boolean Comments){
            if (DatabaseConnection != null){   
                String query = "INSERT INTO COLUMN_DISPLAY_TABLE VALUES ('" 
                + Album + "', '" + Artist + "', '" + Year + "', '" + Genre +
                "', '" + Comments + "')";
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    stmt.execute(query);
                }//end try
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch     
            }//end if    
        }//close AddColumnDisplayTableRow
        
        
        public void AddRecentlyPlayedTableRow(Connection DatabaseConnection,
         String location, String songName){
            if (DatabaseConnection != null){
                String query = "INSERT INTO RECENTLY_PLAYED (FILE_LOCATION, " +
                 "SONG_NAME) VALUES ('" + location + "', '" + songName + "')"; 
                try (Statement stmt = DatabaseConnection.createStatement()) {
                        stmt.execute(query);
                }//end try
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch
            }//end if
        }//close AddRecentlyPlayedTableRow
        
        public int CheckRecentlyPlayedTableSize(Connection DatabaseConnection){
            if (DatabaseConnection != null){
                try {
                    String query = "SELECT COUNT(*) FROM RECENTLY_PLAYED";
                    PreparedStatement statement = DatabaseConnection.
                     prepareStatement(query);
                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        return rs.getInt(1);
                    }
                } //end if
                catch (SQLException ex) {
                    Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return 0;
        }//close CheckRecentlyPlayedTableSize
        
      
        public void DeleteLibraryRow(Connection DatabaseConnection, Object 
         key){
            if (DatabaseConnection != null && key != null){
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM MP3_DATA WHERE";
                    query += " PKEY = " + key + "";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
        }//close DeleteLibraryRow
        
  
        public void DeletePlaylistRow(Connection DatabaseConnection, String 
         playlistName){
            if (DatabaseConnection != null){
                //stmt.execute("DELETE FROM WORKERS WHERE ID=6");
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM PLAYLIST_NAMES_TABLE WHERE";
                    query += " PLAYLIST_NAME = '" + playlistName+ "'";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
        }//close DeletePlaylistRow 
        
        
        public void DeletePlaylistSongsRow(Connection DatabaseConnection, int
         playlistKey){
            if (DatabaseConnection != null){
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM PLAYLIST_SONGS WHERE";
                    query += " PLAYLIST_KEYS = '" + playlistKey+ "'";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
        }//close DeletePlaylistSongsRow        
        
        public void DeleteSongsFromPlaylist(Connection DatabaseConnection, int
         playlistKey, Object songKey){
            if (DatabaseConnection != null){
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM PLAYLIST_SONGS WHERE";
                    query += " PLAYLIST_KEYS = '" + playlistKey+ "' AND "
                     + "SONG_KEYS = '" + songKey + "'";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
            
        }
        
        public void DeleteColumnDisplayTableRow(Connection DatabaseConnection){
            if (DatabaseConnection != null){
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM COLUMN_DISPLAY_TABLE WHERE 1=1";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
        }//close DeletePlaylistRow 
        
        public void DeleteRecentlyPlayedTableRow(Connection DatabaseConnection){
            if (DatabaseConnection != null){
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM RECENTLY_PLAYED "
                     + "WHERE SONG_NUM = (SELECT MIN(SONG_NUM) FROM RECENTLY_PLAYED)";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
        }//close DeleteRecentlyPlayedTableRow
    

        public void RemoveDeletedSongsFromPlaylists(Connection DatabaseConnection,
         Object songKey) {
                    if (DatabaseConnection != null){
                try (Statement stmt = DatabaseConnection.createStatement( )) {
                    String query = "DELETE FROM PLAYLIST_SONGS WHERE";
                    query += " SONG_KEYS = '" + songKey + "'";
                    stmt.execute(query);
                }
                catch(SQLException err) {
                    System.out.println(err.getMessage());
                }//end catch          
            }//end if      
            
        }
        
        
        public Vector GetLibrarySongs(Connection DatabaseConnection)throws 
         Exception {
            Vector<Vector<String>> songVector = new Vector<Vector<String>>();
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             "select * from MP3_DATA");
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
               Vector<String> Song = new Vector<String>();
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
            }//close while loop
            return songVector; 
        }//close getLibrarySongs()
        
        public boolean CheckIfSongExistsInLibrary(Connection DatabaseConnection, 
         String fileName)throws Exception {
            boolean exists = false;
            String query = "select * from MP3_DATA";
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
               if(fileName.equals(rs.getString(1))){
                   exists = true;
                   break;
               }
               else
                   exists = false;
            }
            return exists;
        }
        
        public Object GetLibrarySongKey(Connection DatabaseConnection, String 
         path)throws Exception {
            Object returnValue = null;
            String query = "select * from MP3_DATA WHERE ";
            query += " FILE = '" + path + "'";
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                returnValue = rs.getString(9);
                return returnValue;      
            }
            return returnValue;
        }
        
        
        public Object GetPlayRecentSongPath(Connection DatabaseConnection, String 
         songName)throws Exception {
            Object returnValue = null;
            String query = "select * from RECENTLY_PLAYED WHERE ";
            query += " SONG_NAME = '" + songName + "'";
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                returnValue = rs.getString(2);
                return returnValue;      
            }
            return returnValue;
        }
        
        public boolean ComparePlaylistNames(Connection DatabaseConnection, 
         String proposedPlaylistName)throws Exception {      
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             "select * from PLAYLIST_NAMES_TABLE");
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
               //playlistName.add(rs.getString(1));//PKey
               if(proposedPlaylistName!=null && proposedPlaylistName.equals(
                rs.getString(2)))
                   return false;
               if(proposedPlaylistName!=null && (proposedPlaylistName.
                equals("Library") || proposedPlaylistName.equals("Playlists")))
                   return false;
            }//close while loop
            return true;
        }//close ComparePlaylistNames()   
        
        public void InitializePlaylistTree(Connection DatabaseConnection, 
         DefaultMutableTreeNode node)throws Exception { 
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             "select * from PLAYLIST_NAMES_TABLE");
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
               //playlistName.add(rs.getString(1));//PKey
                node.add(new DefaultMutableTreeNode(rs.getString(2)));
            }//close while loop
        }
        
        public int GetPlaylistKey(Connection DatabaseConnection, 
         String playlistName)throws Exception { 
            int returnValue=-1;
            String query = "select * from PLAYLIST_NAMES_TABLE WHERE";
            query += " PLAYLIST_NAME = '" + playlistName+ "'";
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                returnValue= Integer.parseInt(rs.getString(1));
            }
            return returnValue;
        }
        
        
        public ArrayList<String> GetPlaylistSongNames(Connection DatabaseConnection, int key)
         throws Exception {
            ArrayList<String> songList = new ArrayList<>();
            String query="select * from PLAYLIST_SONGS WHERE PLAYLIST_KEYS = ";
            query += "'" + key+ "'";
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                //System.out.println(rs.getString(2));//Song Key
                songList.add(rs.getString(2));
            }//close while loop 
            return songList;
        }//close getPlaylistNames()     
        
        public ArrayList<String> GetRecentlyPlayedSongNames(Connection 
         DatabaseConnection)throws Exception {
            ArrayList<String> songList = new ArrayList<>();
            String query="select * from RECENTLY_PLAYED" ;
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                //System.out.println(rs.getString(3));//Song Name
                songList.add(rs.getString(3));//Song Name
            }//close while loop 
            return songList;
        }
 
        
        public ArrayList<String> AssignColumnValues(Connection DatabaseConnection, boolean 
         Album, boolean Artist, boolean Year, boolean Genre, boolean Comments)
         throws Exception {
            ArrayList<String> columnValues = new ArrayList<>();
            String query = "select * from COLUMN_DISPLAY_TABLE";
            PreparedStatement statement = DatabaseConnection.prepareStatement(
             query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
               columnValues.add(rs.getString(1)); //Album
               columnValues.add(rs.getString(2)); //Artist
               columnValues.add(rs.getString(3)); //Year
               columnValues.add(rs.getString(4)); //Genre
               columnValues.add(rs.getString(5)); //Comments
                    
            }
            return columnValues;
        }
        
    }//close MP3_Database class
}