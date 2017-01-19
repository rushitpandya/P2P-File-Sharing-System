import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileHandler {
	
	private static final String downloadLocation = "downloads/";
	private static final String replicaLocation = "replica/";
	//private static final int BUFFER_SIZE = 1024 * 64; // 64 KiloBytes
	
	/***
	 * This method retrieves all the files from the given path.
	 * @param path	Path whose files are to be returned
	 * @return		Filename of all the files contained in the given path
	 */
	public static ArrayList<String> getFiles(String path) {
		ArrayList<String> filesArrayList = new ArrayList<String>();
		File folder = new File(path);
		if (folder.isDirectory()) {
			File[] filesList = folder.listFiles();
			
			
			if (filesList != null) {
				for (int i = 0; i < filesList.length; i++) {
					if (filesList[i].isFile()) {
						//String fileLocation = filesList[i].getParent();
						String fileABPath = filesList[i].getAbsolutePath();
						if(!fileABPath.endsWith("~")) {
							filesArrayList.add(fileABPath);
						}	
					}
				}
			}
		} else if (folder.isFile()) {
			filesArrayList.add(folder.getAbsolutePath());
			//files.add(path.substring(path.lastIndexOf("/") + 1, path.length()));
		}
		
		return filesArrayList;
	}

}
