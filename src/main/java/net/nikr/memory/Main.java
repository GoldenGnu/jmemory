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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JOptionPane;

public final class Main {

	public static final String PROGRAM_VERSION = "3.0.0";

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
		execute(getLocalFile(getJarFile()), args);
	}

	private void execute(final String jarFile, final String[] args) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(getJavaHome());
		List<String> commands = new ArrayList<String>();
		if (isWindows()) {
			commands.add("javaw");
		} else {
			commands.add("java");
		}
		commands.add("-Xmx" + getProperties("jmemory.properties", Collections.singletonMap("xmx", "1g")).getProperty("xmx", "1g"));
		commands.add("-jar");
		commands.add(jarFile);
		commands.addAll(Arrays.asList(args));
		processBuilder.command(commands);
		try {
			Process start = processBuilder.start();
			System.exit(0);
		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private String getLocalFile(final String filename) {
		try {
			File dir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			String fixedFilename = dir.getAbsolutePath() + File.separator + filename;
			File file = new File(fixedFilename);
			if (!file.exists()) {
				throw new RuntimeException(fixedFilename + " not found");
			} else {
				return fixedFilename;
			}
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
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
			input = new FileInputStream(filename);
			props.load(input);
			return props;
		} catch (IOException ex) {
			if (defaultValues != null)  {
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
			output = new FileOutputStream(filename);
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
