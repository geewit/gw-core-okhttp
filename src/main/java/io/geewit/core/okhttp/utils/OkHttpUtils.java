package io.geewit.core.okhttp.utils;

import io.geewit.core.okhttp.interceptor.HttpLoggingInterceptor;
import okhttp3.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author geewit
 */
@SuppressWarnings({"unused"})
public class OkHttpUtils {


	private static OkHttpClient httpClient;
	private static OkHttpClient httpsClient;
	private static HttpLoggingInterceptor loggingInterceptor;

	static {
		loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);

		httpClient = new OkHttpClient.Builder()
				.retryOnConnectionFailure(true)
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.addInterceptor(loggingInterceptor)
				.build();

		httpsClient = new OkHttpClient.Builder()
				.retryOnConnectionFailure(true)
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.hostnameVerifier((hostname, session) -> true)
				.addInterceptor(loggingInterceptor)
				.sslSocketFactory(Objects.requireNonNull(createSSLSocketFactory()), createTrustManager())
				.build();
	}

	private static X509TrustManager createTrustManager() {
		return new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
	}

	private static SSLSocketFactory createSSLSocketFactory() {
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null,  new TrustManager[] { createTrustManager() }, new SecureRandom());
			return sc.getSocketFactory();
		} catch (KeyManagementException | NoSuchAlgorithmException ignored) {
			return null;
		}
	}


	private static OkHttpClient determineClient(String url) {
		if(StringUtils.startsWithIgnoreCase(url, "https://")) {
			return httpsClient;
		} else {
			return httpsClient;
		}
	}

	private static OkHttpClient determineClient(URI uri) {
		String scheme = uri.getScheme();
		if(StringUtils.startsWithIgnoreCase(scheme, "https")) {
			return httpsClient;
		} else {
			return httpsClient;
		}
	}

	public static OkHttpClient httpClient() {
		return httpClient;
	}

	public static OkHttpClient httpsClient() {
		return httpsClient;
	}

	public static OkHttpClient buildHttpClient(String proxyHost, int proxyPort) {
		OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.retryOnConnectionFailure(true)
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.hostnameVerifier((hostname, session) -> true)
				.addInterceptor(loggingInterceptor)
				.sslSocketFactory(Objects.requireNonNull(createSSLSocketFactory()), createTrustManager());
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(proxyHost)) {
            httpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
		return httpClientBuilder.build();
	}

	public static Response post(String url, Headers headers, String body, MediaType mediaType) throws IOException {
		RequestBody requestBody = RequestBody.create(mediaType, body);
		Request request = new Request.Builder().url(url).headers(headers).post(requestBody).build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
	}

	public static Response postForm(String url, Headers headers, FormBody body) throws IOException {
		Request.Builder requestBuilder = new Request.Builder().url(url);
		if(headers != null) {
			requestBuilder.headers(headers);
		}
		if(body != null) {
			requestBuilder.post(body);
		}
		Request request = requestBuilder.build();
		Response response = httpClient.newCall(request).execute();
		return response;
	}

	public static Response get(String url) throws IOException {
		Request request = new Request.Builder().url(url).get().build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
	}

    public static Response get(String url, Headers headers) throws IOException {
		Request request = new Request.Builder().url(url).headers(headers).get().build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
    }


	public static Response get(String url, Map<String, String> params) throws IOException {
		String requestUrl = buildUrl(url, params);
		Request request = new Request.Builder().url(url).get().build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
	}

	public static Response get(String url, Map<String, String> params, Cookie cookie) throws IOException {
		String requestUrl = buildUrl(url, params);
		return get(requestUrl, cookie);
	}

    public static Response get(String url, Map<String, String> params, String cookieName, String cookieValue) throws IOException {
        String requestUrl = buildUrl(url, params);
        return get(requestUrl, cookieName, cookieValue);
    }

    public static String buildUrl(String url, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if(params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            Iterator<Map.Entry<String, String>> entryIterator = params.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> entry = entryIterator.next();
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                if(entryIterator.hasNext()) {
                    urlBuilder.append("&");
                }
            }
        }
        String requestUrl = urlBuilder.toString();
        return requestUrl;
    }

	public static Response get(String url, Cookie cookie) throws IOException {
		Request request = new Request.Builder().url(url).addHeader(HttpHeaders.COOKIE, cookie.name() + "=" + cookie.name()).get().build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
	}

    public static Response get(String url, String cookieName, String cookieValue) throws IOException {
        Request request = new Request.Builder().url(url).addHeader(HttpHeaders.COOKIE, cookieName + "=" + cookieValue).get().build();
		OkHttpClient httpClient = determineClient(url);
        Response response = httpClient.newCall(request).execute();
        return response;
    }

	public static Response postJson(String url, String body) throws IOException {
		MediaType mediaType = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
		RequestBody requestBody = RequestBody.create(mediaType, body);
		Request request = new Request.Builder().url(url).post(requestBody).build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
	}

	public static Response request(URI uri, HttpMethod httpMethod, Cookie cookie, Map<String, String> params, String body) throws IOException {
		MediaType mediaType = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
		RequestBody requestBody = RequestBody.create(mediaType, body);
		Request.Builder requestBuilder = new Request.Builder().url(uri.toString());
		if(cookie != null) {
			requestBuilder.addHeader(HttpHeaders.COOKIE, cookie.name() + "=" + cookie.name());
		}
		Request request = requestBuilder.method(httpMethod.name(), requestBody).build();
		OkHttpClient httpClient = determineClient(uri);
		Response response = httpClient.newCall(request).execute();
		return response;
	}

	public static Response request(String url, HttpMethod httpMethod, Cookie cookie, Map<String, String> params, String body) throws IOException {
		MediaType mediaType = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
		RequestBody requestBody = RequestBody.create(mediaType, body);
		Request.Builder requestBuilder = new Request.Builder().url(url);
		if(cookie != null) {
			requestBuilder.addHeader(HttpHeaders.COOKIE, cookie.name() + "=" + cookie.name());
		}
		Request request = requestBuilder.method(httpMethod.name(), requestBody).build();
		OkHttpClient httpClient = determineClient(url);
		Response response = httpClient.newCall(request).execute();
		return response;
	}
}
