package com.zhukai.project.partner.server.wrapper;

/**
 * Created by homolo on 17-8-4.
 */
public class FileBean {
	private String name;
	private boolean file;
	private String lastModified;
	private String displaySize;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getDisplaySize() {
		return displaySize;
	}

	public void setDisplaySize(String displaySize) {
		this.displaySize = displaySize;
	}
}
