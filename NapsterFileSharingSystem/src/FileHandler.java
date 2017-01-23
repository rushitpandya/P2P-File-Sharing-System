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
	
	public static final String downloadLocation = "downloads/";
	public static final String replicaLocation = "replica/";
	//private static final int BUFFER_SIZE = 1024 * 64; // 64 KiloBytes
	
	/***
	 * This method retrieves all the files from the given path.
	 * @param path	Path whose files are to be returned
	 * @return		Filename of all the files contained in the given path
	 */
	public static ArrayList<FileInfo> getFiles(String path) {
		ArrayList<FileInfo> filesArrayList = new ArrayList<FileInfo>();
		File folder = new File(path);
		if (folder.isDirectory()) {
			File[] filesList = folder.listFiles();
			
			
			if (filesList != null) {
				for (int i = 0; i < filesList.length; i++) {
					if (filesList[i].isFile()) {
						String fileLocation = filesList[i].getAbsolutePath().substring(0,(filesList[i].getAbsolutePath()).lastIndexOf(File.separator));
						String fileName = filesList[i].getName();
						if(!fileLocation.endsWith("~")) {
							FileInfo fi=new FileInfo();
							fi.setFileName(fileName);
							fi.setFileLocation(fileLocation);
							filesArrayList.add(fi);
						}	
					}
					else{
						 ArrayList<FileInfo> afi = getFiles(filesList[i].getPath());
						 for(FileInfo f : afi){
							 filesArrayList.add(f);
						 }
					}
				}
			}
		} else if (folder.isFile()) {
			String fileLocation = folder.getAbsolutePath().substring(0,(folder.getAbsolutePath()).lastIndexOf(File.separator));
			String fileName = folder.getName();
			FileInfo fi=new FileInfo();
			fi.setFileName(fileName);
			fi.setFileLocation(fileLocation);
			filesArrayList.add(fi);
		}
		
		return filesArrayList;
	}

}
