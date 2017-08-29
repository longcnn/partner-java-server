package com.zhukai.project.partner.server.web;

import com.zhukai.framework.fast.rest.FastRestApplication;
import com.zhukai.framework.fast.rest.annotation.core.Autowired;
import com.zhukai.framework.fast.rest.annotation.web.*;
import com.zhukai.framework.fast.rest.common.MultipartFile;
import com.zhukai.framework.fast.rest.common.RequestType;
import com.zhukai.project.partner.server.schedule.WXBatcher;
import com.zhukai.project.partner.server.util.RestClientException;
import com.zhukai.project.partner.server.util.RestClientUtil;
import com.zhukai.project.partner.server.wrapper.RestResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;

import static com.zhukai.project.partner.server.WXConstants.*;

@RestController
@RequestMapping("/wx")
public class WXController {
	private static final Logger logger = LoggerFactory.getLogger(WXController.class);
	private static final String errAnswer = "小Q没听清呢[委屈]";
	private static final String overTimesAnswer = "明天再来找我吧，小Q太累了[睡觉]";

	@Autowired
	private WXBatcher wxBatcher;

	@RequestMapping(value = "/getOpenid/{code}", method = RequestType.GET)
	public String getOpenid(@PathVariable("code") String code) throws RestClientException, IOException {
		String url = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code&appid=" + WX_APP_ID + "&secret=" + WX_SECRET_KEY + "&js_code=" + code;
		return RestClientUtil.executeAsJson(new HttpGet(url)).getString("openid");
	}

	@RequestMapping(value = "/robot", method = RequestType.POST)
	public JSONObject robot(@RequestBody JSONObject body) throws RestClientException, IOException {
		String username;
		try {
			username = body.getString("username");
		} catch (JSONException je) {
			username = "unknow";
		}
		logger.info("{} : {}", username, body.getString("info"));
		body.put("key", TL_API_KEY);
		JSONObject result = RestClientUtil.postJson(TL_API_URL, body);
		logger.info("小Q : {}", result.getString("text"));
		return result;
	}

	@RequestMapping(value = "/uploadSilk", method = RequestType.POST)
	public Object uploadSilk(@RequestParam("file") MultipartFile partFile, @RequestParam("userid") MultipartFile useridPart, @RequestParam("username") MultipartFile usernamePart) throws Exception {
		String userid = new String(useridPart.getBytes(), "utf-8");
		String username = new String(usernamePart.getBytes(), "utf-8");
		username = StringUtils.isBlank(username) ? "unknow" : username;
		logger.debug("id = {} ,name = {}", userid, username);
		String dataDir = FastRestApplication.getStaticPath();
		File file = new File(dataDir + userid + ".silk");
		partFile.transferTo(file);
		logger.debug("upload file success, fileName= {}", file.getName());
		StringBuilder cmdBuilder = new StringBuilder();
		cmdBuilder.append("cd ").append(SILK_V3_DECODER_DIR).append(" && ").append("sh converter.sh ").append(file.getPath()).append(" mp3").append(" && ").append("ffmpeg -i ").append(dataDir).append(userid)
				.append(".mp3 -acodec pcm_s16le -ac 1 -ar 16000 ").append(dataDir).append(userid).append(".wav");
		logger.debug("exec shell: {}", cmdBuilder);
		String[] cmd = new String[]{"/bin/sh", "-c", cmdBuilder.toString()};
		if (Runtime.getRuntime().exec(cmd).waitFor() != 0) {
			throw new Exception("shell脚本执行错误");
		}
		file.delete();
		new File(dataDir + userid + ".mp3").delete();
		File wavFile = new File(dataDir + userid + ".wav");
		InputStream inputStream = new FileInputStream(wavFile);
		byte[] bytes = new byte[(int) wavFile.length()];
		inputStream.read(bytes);
		inputStream.close();
		wavFile.delete();
		String base64Data = Base64.getEncoder().encodeToString(bytes);
		JSONObject body = new JSONObject();
		body.put("format", "wav");
		body.put("rate", 16000);
		body.put("channel", 1);
		body.put("token", wxBatcher.getAccessToken());
		body.put("cuid", "zhukai");
		body.put("len", bytes.length);
		body.put("speech", base64Data);
		inputStream.close();
		wavFile.delete();
		JSONObject result = RestClientUtil.postJson(BAIDU_SPEECH_RECOGNITION_URL, body);
		logger.debug(result.toString());
		int errNo = result.getInt("err_no");
		if (errNo == 3301) {
			return new RestResult(102, errAnswer);
		} else if (errNo == 0) {
			String speechStr = result.getJSONArray("result").getString(0);
			if ("，".equals(speechStr)) {
				return new RestResult(102, errAnswer);
			} else {
				logger.info(username + "（语音）: " + speechStr);
				body = new JSONObject();
				body.put("key", TL_API_KEY);
				body.put("info", speechStr);
				body.put("userid", userid);
				result = RestClientUtil.postJson(TL_API_URL, body);
				logger.debug(result.toString());
				if (result.getInt("code") == 40004) {
					result.put("text", overTimesAnswer);
				}
				String text = result.getString("text");
				if (text.length() > 341) {
					logger.info("小Q: {}", text);
					return new RestResult(102, text);
				}
				String url = BAIDU_SPEECH_COMPOSITION_URL + "?lan=zh&ctp=1&tex=" + URLEncoder.encode(URLEncoder.encode(text, "utf-8"), "utf-8") + "&cuid=" + userid + "&tok=" + wxBatcher.getAccessToken();
				HttpEntity httpEntity = RestClientUtil.execute(new HttpGet(url)).getEntity();
				if (httpEntity.getContentType().getValue().equals("audio/mp3")) {
					String fileName = "speech/" + userid + System.currentTimeMillis() + ".mp3";
					httpEntity.writeTo(new FileOutputStream(new File(dataDir + fileName)));
					logger.info("小Q（语音）: {}", text);
					return new RestResult(101, fileName);
				} else {
					logger.error("Speech composition result: {}", EntityUtils.toString(httpEntity, "utf-8"));
				}
			}
		}
		throw new RuntimeException("Get answer error");
	}


}
