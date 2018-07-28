/*
 * Copyright 2014 Niklas Kyster Rasmussen
 *
 * This file is part of jMemory.
 *
 * jMemory is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jMemory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jMemory; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package net.nikr.memory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JOptionPane;

public final class Main {

	public static final String PROGRAM_VERSION = "4.1.0";

	public static final String DEFAULT_XMX_VALUE = "1g";
	public static final String EXIT_ON = "jmemory ok";

	/**
	 * Entry point for jMemory.
	 *
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		NikrUncaughtExceptionHandler.install();
		Main main = new Main();
		main.work(args);
	}

	public static String getJarFile() {
		return getProperties("jarfile.properties", null).getProperty("jarfile", "No config");
	}

	private Main() {
		
	}

	private void work(final String[] args) {
		String filename = getJarFile();
		String path = getLocalFile(filename);
		File file = new File(path);
		if (!file.exists()) {
			throw new RuntimeException(filename + " not found");
		}
		execute(path, args);
		
	}

	private void execute(final String jarFile, final String[] args) {
		if (System.getProperty("sun.arch.data.model").equals("32")) {
			int value = JOptionPane.showConfirmDialog(null,
					"Looks like you're using Java 32bit.\r\n"
					+ "You should switch to Java 64bit (64x),\r\n"
					+ "instead of using jmemory.jar\r\n"
					+ "\r\n"
					+ "Continue anyway?"
					+ "\r\n"
					+ "\r\n",
					"jMemory", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (value == JOptionPane.NO_OPTION) {
				System.exit(0);
			}
		}
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(getJavaHome());
		List<String> commands = new ArrayList<String>();
		if (isWindows()) {
			commands.add("javaw");
		} else {
			commands.add("java");
		}
		commands.add("-Xmx" + getProperties("jmemory.properties", Collections.singletonMap("xmx", DEFAULT_XMX_VALUE)).getProperty("xmx", DEFAULT_XMX_VALUE));
		commands.add("-jar");
		commands.add(jarFile);
		commands.add("-jmemory");
		commands.addAll(Arrays.asList(args));
		processBuilder.command(commands);
		try {
			Process start = processBuilder.start();
			String output = processOutput(start);
			if (output != null && !output.isEmpty()) {
				JOptionPane.showMessageDialog(null, "JVM ERROR:\r\n" + output+ "\r\n\r\nPress OK to close jMemory\r\n\r\n", "jMemory", JOptionPane.ERROR_MESSAGE);
			}
			System.exit(0);
		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static String processOutput(Process start) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(start.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if (line.toLowerCase().contains(EXIT_ON.toLowerCase())) {
					return null; //exit;
				}
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return sb.toString();
	}

	private static String getLocalFile(final String filename) {
		File file;
		URL location = net.nikr.memory.Main.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			file = new File(location.toURI());
		} catch (Exception ex) {
			file = new File(location.getPath());
		}
		return file.getParentFile().getAbsolutePath() + File.separator + filename;
	}

	private static File getJavaHome() {
		return new File(System.getProperty("java.home") + File.separator + "bin");
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	private static Properties getProperties(String filename, Map<String, String> defaultValues) {
		Properties props = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(getLocalFile(filename));
			props.load(input);
			return props;
		} catch (IOException ex) {
			if (defaultValues != null) {
				for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
					props.setProperty(entry.getKey(), entry.getValue());
				}
				saveProperties(filename, props);
				return props;
			} else {
				JOptionPane.showMessageDialog(null, filename + " not found", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					//I give up...
				}
			}
		}
	}

	private static void saveProperties(String filename, Properties props) {
		OutputStream output = null;
		try {
			output = new FileOutputStream(getLocalFile(filename));
			props.store(output, "");
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, filename + " cloud not be saved", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					//I give up...
				}
			}
		}
	}
}
