package io.geewit.core.okhttp.configuration;

import io.geewit.core.okhttp.utils.OkHttpUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplateCustomizer restTemplateCustomizer() {
        return restTemplate -> restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(OkHttpUtils.httpClient()));
    }
}
