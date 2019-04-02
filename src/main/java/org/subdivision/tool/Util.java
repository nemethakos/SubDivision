package org.subdivision.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.subdivision.Image;

public class Util {

	
	public static String[] splitPath(String pathString) {
		Path path = Paths.get(pathString);
		return StreamSupport.stream(path.spliterator(), false).map(Path::toString).toArray(String[]::new);
	}

	public static JSONObject getJSONObjectFromClassPath(String name) throws ClassNotFoundException, ParseException {
		String configuration = getResourceFromClassPath(name);
		// System.out.println(configuration);
		var parser = new JSONParser();
		var obj = parser.parse(configuration);
		var jsonObj = (JSONObject) obj;

		return jsonObj;
	}

	public static void main(String[] args) throws ClassNotFoundException, ParseException {

		System.out.println(new Configuration());

	}

	/**
	 * Loads text file from the class path
	 * 
	 * @param name the name of the file
	 * @return the {@link String} with the file's content
	 * @throws ClassNotFoundException
	 */
	public static String getResourceFromClassPath(String name) throws ClassNotFoundException {
		var resourceStream = Util.class.getClassLoader().getResourceAsStream(name);
		var br = new BufferedReader(new InputStreamReader(resourceStream));
		var content = br.lines().collect(Collectors.joining("\r\n"));
		return content;
	}

    public static String getFileNameFrom(String fileNameWithExtension) {
        String[] tokens = fileNameWithExtension.split("\\.(?=[^\\.]+$)");
        String[] fileNamePath = splitPath(tokens[0]);
        return fileNamePath[fileNamePath.length - 1];
    }

    public static String getDateString() {
        var sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String dateStr = sdf.format(new Date());
        return dateStr;
    }

    public static void saveImageTo(Image image, String path, String fileName) {
    
        String dateStr = getDateString();
    
        String fileNameWithoutExt = getFileNameFrom(fileName);
    
        String outputFileName = path + "/" + fileNameWithoutExt + "_" + dateStr + ".png";
    
        File f = new File(outputFileName);
        f.getParentFile().mkdirs();
    
        image.saveImage(outputFileName);
    }

}
