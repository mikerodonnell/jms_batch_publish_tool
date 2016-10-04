package demo;

import java.io.File;
import java.io.FilenameFilter;

/**
 * FilenameFilter implementation to exclude hidden files, such as OSX's .DS_Store files
 * 
 * @author Mike O'Donnell  github.com/mikerodonnell
 */
public class HiddenFileFilter implements FilenameFilter {

	@Override
	public boolean accept( final File directory, final String name ) {
		return !name.startsWith(".");
	}

}
