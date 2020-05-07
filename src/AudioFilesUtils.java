package be.regisandre.synth;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AudioFilesUtils {
	public ArrayList<String> sortByNumber(File folder) {
		List<String> fileList = new ArrayList<String>();
		File[] listOfFiles = folder.listFiles();

		Arrays.sort(listOfFiles, new Comparator<File>() {
			public int compare(File o1, File o2) {
				int n1 = extractNumber(o1.getName());
				int n2 = extractNumber(o2.getName());
				return n1 - n2;
			}

			private int extractNumber(String name) {
				int i = 0;

				try {
					int s = name.indexOf('_') + 1;
					int e = name.lastIndexOf('.');
					String number = name.substring(s, e);
					i = Integer.parseInt(number);
				} catch (Exception e) {
					i = 0;
				}

				return i;
			}
		});

		for (File f : listOfFiles) {
			if (f.isFile()) {
				fileList.add(f.getName().replace(".fcs", ""));
			}
		}

		return (ArrayList<String>) fileList;
	}

	public String getMaxNumber(ArrayList<String> listFiles) {
		return listFiles.get(listFiles.size() - 1).toString();
	}

	public int countFiles(File folder) {
		File[] f = folder.listFiles();
		int x = 0;
		
		for (int i = 0; i < f.length; i++) {
			if (f[i].isFile()) {
				x++;
			}
		}

		return x;
	}
	
	public File getLastModifiedFile(String folder) {
	    File fl = new File(folder);
	    File[] files = fl.listFiles(new FileFilter() {          
	        public boolean accept(File file) {
	            return file.isFile();
	        }
	    });
	    
	    long lastMod = Long.MIN_VALUE;
	    File lastMofidiedFile = null;
	    
	    for (File file : files) {
	        if (file.lastModified() > lastMod) {
	        	lastMofidiedFile = file;
	            lastMod = file.lastModified();
	        }
	    }
	    
	    return lastMofidiedFile;
	}
}
