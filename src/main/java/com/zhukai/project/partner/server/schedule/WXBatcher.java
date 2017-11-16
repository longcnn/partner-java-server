package com.zhukai.project.partner.server.schedule;

import com.zhukai.framework.fast.rest.annotation.core.Component;
import com.zhukai.framework.fast.rest.annotation.extend.Scheduled;
import com.zhukai.project.partner.server.WXConstants;
import com.zhukai.project.partner.server.util.RestClientException;
import com.zhukai.project.partner.server.util.RestClientUtil;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.IOException;

@Component
public class WXBatcher {

	private String accessToken;

	@Scheduled(cron = "0 0 2 ? * *")
	public void requestAccessToken() throws RestClientException, IOException {
		String url = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + WXConstants.BAIDU_API_KEY + "&client_secret=" + WXConstants.BAIDU_SECRET_KEY;
		HttpGet get = new HttpGet(url);
		JSONObject result = RestClientUtil.executeAsJson(get);
		accessToken = result.getString("access_token");
	}

	public String getAccessToken() {
		return accessToken;
	}
}
