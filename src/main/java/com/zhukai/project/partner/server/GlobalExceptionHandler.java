package com.zhukai.project.partner.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhukai.framework.fast.rest.annotation.web.ControllerAdvice;
import com.zhukai.framework.fast.rest.annotation.web.ExceptionHandler;
import com.zhukai.framework.fast.rest.common.HttpStatus;
import com.zhukai.framework.fast.rest.http.HttpResponse;
import com.zhukai.project.partner.server.wrapper.RestResult;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	public RestResult exceptionHandler(Exception e, HttpResponse response) {
		logger.error("error", e);
		response.setStatus(HttpStatus.InternalServerError);
		return new RestResult(-1, "Server Error");
	}
}
