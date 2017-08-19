package com.zhukai.project.partner.server;

import com.zhukai.framework.fast.rest.annotation.core.Component;
import com.zhukai.framework.fast.rest.annotation.core.Initialize;

@Component
public class ProjectInit {

	@Initialize
	public void init() {
		System.out.println("do something ...");
	}
}
