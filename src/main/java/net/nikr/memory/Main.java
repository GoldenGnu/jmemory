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
import java.io.IOException;
import java.net.URISyntaxException;


public final class Main {

	/**
	 * Entry point for jMemory.
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		Main main = new Main();
		main.work();
	}

	private Main() { }

	private void work() {
		String jarFile = getLocalFile("jeveassets.jar");
		if (jarFile != null) {
			execute(jarFile);
		}
		System.exit(0);
	}

	private void execute(String jarFile) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(getJavaHome());
		String[] commands = {"java", "-Xmx4g", "-jar", jarFile};
		processBuilder.command(commands);
		try {
			Process process = processBuilder.start();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private String getLocalFile(String filename) {
		try {
			File dir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			return dir.getAbsolutePath() + File.separator + filename;
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	private File getJavaHome() {
		return new File(System.getProperty("java.home") + File.separator + "bin");
	}
}