package com.zhukai.project.partner.server.web;

import com.zhukai.framework.fast.rest.FastRestApplication;
import com.zhukai.framework.fast.rest.annotation.web.RequestMapping;
import com.zhukai.framework.fast.rest.annotation.web.RequestParam;
import com.zhukai.framework.fast.rest.annotation.web.RestController;
import com.zhukai.framework.fast.rest.common.FileEntity;
import com.zhukai.framework.fast.rest.common.MultipartFile;
import com.zhukai.framework.fast.rest.common.RequestType;
import com.zhukai.framework.fast.rest.util.Resources;
import com.zhukai.project.partner.server.util.FileUtils;
import com.zhukai.project.partner.server.wrapper.FileBean;
import com.zhukai.project.partner.server.wrapper.RestResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@RequestMapping("/list")
	public List<FileBean> list(@RequestParam("path") String path) {
		boolean isRoot = StringUtils.isBlank(path);
		path = isRoot ? FastRestApplication.getStaticPath() : FastRestApplication.getStaticPath() + path;
		List<FileBean> fileList = new ArrayList<>();
		File directory = new File(path);
		if (!isRoot) {
			FileBean fileBean = FileUtils.convertFileBean(directory);
			fileBean.setName("../");
			fileList.add(fileBean);
		}
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				fileList.add(FileUtils.convertFileBean(file));
			}
		}
		fileList.sort((v1, v2) -> {
			if (!v1.isFile() && v2.isFile()) {
				return -1;
			} else if (v1.isFile() && !v2.isFile()) {
				return 1;
			} else {
				return 0;
			}
		});
		return fileList;
	}

	@RequestMapping(value = "/download", method = RequestType.GET)
	public FileEntity download(@RequestParam("fileName") String fileName) throws FileNotFoundException {
		if (fileName.charAt(fileName.length() - 1) == '/') {
			File zipFile = FileUtils.zipCompress(FastRestApplication.getStaticPath() + fileName);
			FileEntity zipFileEntity = new FileEntity(zipFile.getName(), new FileInputStream(zipFile));
			zipFile.delete();
			return zipFileEntity;
		} else {
			File file = Resources.getResourceByStatic(fileName);
			return new FileEntity(file.getName(), new FileInputStream(file));
		}
	}

	@RequestMapping("/upload")
	public RestResult upload(@RequestParam("url") MultipartFile urlPart, @RequestParam("file_data") MultipartFile partFile) {
		try {
			String path = FastRestApplication.getStaticPath() + new String(urlPart.getBytes(), "utf-8");
			File file = new File(path + partFile.getOriginalFilename());
			partFile.transferTo(file);
			return new RestResult(1, "file upload success");
		} catch (Exception e) {
			logger.error("upload fail", e);
			return new RestResult(2, "file upload fail");
		}
	}

}
