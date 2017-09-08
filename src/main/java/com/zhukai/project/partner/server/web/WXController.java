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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	private static final String unknowAnswer = "小Q没听清呢[委屈]";
	private static final String errAnswer = "待我休息片刻[睡觉]，决战到天亮啊～";
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
	public Object uploadSilk(@RequestParam("file") MultipartFile partFile, @RequestParam("userid") MultipartFile useridPart, @RequestParam("username") MultipartFile usernamePart) {
		File silkFile = null;
		File tmpWavFile = null;
		InputStream tmpInputStream = null;
		OutputStream outputStream = null;
		try {
			String userid = new String(useridPart.getBytes(), "utf-8");
			String username = new String(usernamePart.getBytes(), "utf-8");
			username = StringUtils.isBlank(username) ? "unknow" : username;
			logger.debug("id = {} ,name = {}", userid, username);
			String dataDir = FastRestApplication.getStaticPath();
			silkFile = new File(dataDir + "tmp/" + userid + ".silk");
			partFile.transferTo(silkFile);
			logger.debug("upload file success, fileName= {}", silkFile.getName());
			String cmd = "cd " + SILK_V3_DECODER_DIR + " && sh converter.sh " + silkFile.getPath() + " mp3 && ffmpeg -i " + dataDir + "tmp/" + userid +
					".mp3 -acodec pcm_s16le -ac 1 -ar 16000 " + dataDir + "tmp/" + userid + ".wav";
			logger.debug("exec shell: {}", cmd);
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", cmd);
			Process process = pb.start();
			if (process.waitFor() != 0) {
				throw new IOException("shell exec fail");
			}
			new File(dataDir + "tmp/" + userid + ".mp3").delete();
			tmpWavFile = new File(dataDir + "tmp/" + userid + ".wav");
			tmpInputStream = new FileInputStream(tmpWavFile);
			byte[] bytes = new byte[(int) tmpWavFile.length()];
			tmpInputStream.read(bytes);
			String base64Data = Base64.getEncoder().encodeToString(bytes);
			JSONObject body = new JSONObject();
			body.put("format", "wav");
			body.put("rate", 16000);
			body.put("channel", 1);
			body.put("token", wxBatcher.getAccessToken());
			body.put("cuid", "zhukai");
			body.put("len", bytes.length);
			body.put("speech", base64Data);
			JSONObject result = RestClientUtil.postJson(BAIDU_SPEECH_RECOGNITION_URL, body);
			logger.debug(result.toString());
			int errNo = result.getInt("err_no");
			if (errNo == 3301) {
				return new RestResult(102, unknowAnswer);
			} else if (errNo == 0) {
				String speechStr = result.getJSONArray("result").getString(0);
				if ("，".equals(speechStr)) {
					return new RestResult(102, unknowAnswer);
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
						outputStream = new FileOutputStream(new File(dataDir + fileName));
						httpEntity.writeTo(outputStream);
						outputStream.flush();
						logger.info("小Q（语音）: {}", text);
						return new RestResult(101, fileName);
					} else {
						logger.error("Speech composition result: {}", EntityUtils.toString(httpEntity, "utf-8"));
					}
				}
			}
			return new RestResult(102, errAnswer);
		} catch (Exception e) {
			logger.error("Speech dialogue error", e);
			return new RestResult(102, errAnswer);
		} finally {
			FileUtils.deleteQuietly(silkFile);
			FileUtils.deleteQuietly(tmpWavFile);
			IOUtils.closeQuietly(tmpInputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

}
