package jacusa.cli.options;


import jacusa.cli.parameters.SampleParameters;

import java.io.File;
import java.io.FileNotFoundException;

public class PathnameArg {

	public static final char SEP = ',';

	private SampleParameters parameters;
	
	public PathnameArg(SampleParameters paramteres) {
		this.parameters = paramteres;
	}

	public void processArg(String arg) throws Exception {
		String[] pathnames = arg.split(Character.toString(SEP));
    	for (String pathname : pathnames) {
	    	File file = new File(pathname);
	    	if (! file.exists()) {
	    		throw new FileNotFoundException("File (" + pathname + ") in not accessible!");
	    	}
    	}
    	// beware of ugly code
		parameters.setPathnames(pathnames);
	}

}
