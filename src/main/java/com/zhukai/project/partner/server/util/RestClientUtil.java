package com.zhukai.project.partner.server.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.zhukai.framework.fast.rest.util.JsonUtil;

public class RestClientUtil {
	private static final String DEFAULT_CHARSET = "UTF-8";

	public static JSONObject executeAsJson(HttpRequestBase request, String charset) throws RestClientException, IOException {
		HttpResponse response = execute(request);
		String result = EntityUtils.toString(response.getEntity(), charset);
		return JsonUtil.convertObj(result, JSONObject.class);
	}

	public static JSONObject executeAsJson(HttpRequestBase request) throws RestClientException, IOException {
		return executeAsJson(request, DEFAULT_CHARSET);
	}

	public static HttpResponse execute(HttpRequestBase request) throws RestClientException, IOException {
		try {
			HttpClient client = request.getRequestLine().getUri().startsWith("https") ? getSSLRestClient() : HttpClientBuilder.create().build();
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RestClientException();
			}
			return response;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RestClientException(e);
		}
	}

	private static HttpClient getSSLRestClient() throws NoSuchAlgorithmException, KeyManagementException {
		RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
		registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { getTrustManager() }, null);
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		Registry<ConnectionSocketFactory> registry = registryBuilder.register("https", socketFactory).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
		return HttpClientBuilder.create().setConnectionManager(connectionManager).build();
	}

	private static TrustManager getTrustManager() {
		return new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
	}
}
