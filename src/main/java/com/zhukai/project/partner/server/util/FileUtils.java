package com.zhukai.project.partner.server.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import com.zhukai.project.partner.server.wrapper.FileBean;

public class FileUtils {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static File zipCompress(String path) throws FileNotFoundException {
		File src = new File(path);
		if (!src.exists()) {
			throw new FileNotFoundException(path + " not exists");
		}
		path = path.charAt(path.length() - 1) == '/' ? path.substring(0, path.length() - 1) : path;
		File zipFile = new File(path + ".zip");
		Project prj = new Project();
		Zip zip = new Zip();
		zip.setProject(prj);
		zip.setDestFile(zipFile);
		FileSet fileSet = new FileSet();
		fileSet.setProject(prj);
		fileSet.setDir(src);
		zip.addFileset(fileSet);
		zip.execute();
		return zipFile;
	}

	public static FileBean convertFileBean(File file) {
		FileBean fileBean = new FileBean();
		fileBean.setFile(file.isFile());
		fileBean.setName(file.isFile() ? file.getName() : file.getName() + "/");
		fileBean.setLastModified(displayDate(file.lastModified()));
		fileBean.setDisplaySize(displayFileSize(file.length()));
		return fileBean;
	}

	private static String displayFileSize(long size) {
		if (size < 1024) {
			return size + " B";
		}
		double result = (double) size / 1024;
		if (result < 1024) {
			return DECIMAL_FORMAT.format(result) + " KB";
		} else if (result < 1024 * 1024) {
			return DECIMAL_FORMAT.format(result / 1024) + " M";
		} else {
			return DECIMAL_FORMAT.format(result / 1024 / 1024) + " G";
		}
	}

	private static String displayDate(long time) {
		return DATE_FORMAT.format(new Date(time));
	}

}
