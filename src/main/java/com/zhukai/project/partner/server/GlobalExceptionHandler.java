package com.zhukai.project.partner.server;

import com.zhukai.framework.fast.rest.annotation.web.ControllerAdvice;
import com.zhukai.framework.fast.rest.annotation.web.ExceptionHandler;
import com.zhukai.project.partner.server.wrapper.RestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	public RestResult exceptionHandler(Exception e) {
		logger.error("error", e);
		return new RestResult(-1, "Server Error");
	}
}
