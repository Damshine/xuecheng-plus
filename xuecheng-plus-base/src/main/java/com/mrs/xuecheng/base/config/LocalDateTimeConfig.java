package com.mrs.xuecheng.base.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * description: LocalDateTimeConfig
 * date: 2023/4/13 10:26
 * author: MR.孙
 */
@Configuration
public class LocalDateTimeConfig {

    /**
     * 序列化
     * @return
     */
    @Bean
    public LocalDateTimeSerializer localDateTimeSerializer() {

        return new LocalDateTimeSerializer(DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss"));

    }

    /**
     * 反序列化
     * @return
     */
    @Bean
    public LocalDateTimeDeserializer localDateTimeDeserializer() {
        return new LocalDateTimeDeserializer(DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, localDateTimeSerializer());
            builder.deserializerByType(LocalDateTime.class, localDateTimeDeserializer());
        };
    }


}
