Project Overview:  
Functional MP3 Player with custum GUI.  
  
For this project, two other individuals and I created a functional MP3 player interface. We created a custom Graphical User Interface (GUI) for our MP3 player and used the mp3agic library to read mp3 files and read/manipulate the ID3 tags to display song information in our GUI. Song information is stored in a table within our MP3_Database database. This table is used to keep track of all of the song information and display it within our GUI.   
  
Our MP3 Player can successfully play, pause, and stop songs. Users are also given the options to skip to the next song or skip to the previous song. Users can also add songs to their library through the file menu or by simply dragging and dropping the desired mp3 file in the area where all of the other songs are displayed. Deleting songs is just as easy. Users can either select the song they want deleted and delete it through the file menu or they can simply right-click the song and choose to delete it from the library.   
  
Playlists are also implemented into our MP3 player where users can easily create and delete playlists as well as add and delete songs from a playlist.  
  
Users can also conveniently increase/decrease the volume of the songs being played using the volume slider in the upper left of the GUI or by using the Control menu. Users can also choose to repeat songs after they finish and shuffle the songs in their library by clicking the appropriate options in the Control menu. These features take effect once a song finishes playing.   
  
The song information area is also user-customizable where they can choose what song information is viewed by right-clicking the header in the song area. They can choose between displaying the artist, album name, release year, genre, and comments within the ID3 tags of the song files.  
  
Finally, users can track the progress of the song being played by viewing the song progress bar at the very bottom of the GUI.  
Here is what our final GUI looks like:  
![MP3_Player_Screenshot](https://user-images.githubusercontent.com/14812721/133513706-990836e2-1d4a-41c9-8b56-119351acb083.jpg)

  

Dependencies:  
This project was created using the NetBeans IDE Version 8.0.2.  
  
Project Verification:   
In order to run this program, you first need to connect to the MP3_Player_Database Database in the Services tab. This is necessary to be able to add/remove/play songs within the MP3 interface. Once you are connected to the database you should be able to run the program with ease.
