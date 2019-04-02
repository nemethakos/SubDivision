package org.subdivision.tool;

import java.io.File;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Configuration {

	private String imageDir;

	private String outputDir;

	private String videoDir;
	private static Configuration INSTANCE = new Configuration();

	public static Configuration getInstance() {
		return INSTANCE;
	}

	public Configuration() {
		super();

		var map = new JSONObject();
		try {
			map = Util.getJSONObjectFromClassPath("config.json");
		} catch (ClassNotFoundException | ParseException e) {
			e.printStackTrace();
		}

		imageDir = (String) map.get("inputDir");
		videoDir = (String) map.get("videoDir");
		outputDir = (String) map.get("outputDir");
	}

	public String getImageDir() {
		return imageDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public String getVideoDir() {
		return videoDir;
	}

	public String getFullVideoFileName(String fileName) {
		String fileNameString = Paths.get(getVideoDir(), fileName).toString();
		File f = new File(fileNameString);
		String absolutePath = f.getAbsolutePath();
		System.out.println(absolutePath);
		return absolutePath;
	}
	
	public String getFullInputImageFileName(String fileName) {
		String fileNameString = Paths.get(getImageDir(), fileName).toString();
		File f = new File(fileNameString);
		String absolutePath = f.getAbsolutePath();
		System.out.println(absolutePath);
		return absolutePath;
	}

	public String getFullOutputDir(String fileName) {
		String fileNameString = Paths.get(getOutputDir(), fileName).toString();
		File f = new File(fileNameString);
		String absolutePath = f.getParentFile().getAbsolutePath();
		return absolutePath;
	}
	
	@Override
	public String toString() {
		return String.format("Configuration [imageDir=%s, videoDir=%s, outputDir=%s]", imageDir, videoDir, outputDir);
	}

}
