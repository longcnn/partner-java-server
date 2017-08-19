package com.zhukai.project.partner.server;

import com.zhukai.framework.fast.rest.FastRestApplication;
import com.zhukai.framework.fast.rest.annotation.core.EnableStaticServer;

@EnableStaticServer("/data")
public class Application {
	public static void main(String[] args) {
		FastRestApplication.run(Application.class);
	}
}
