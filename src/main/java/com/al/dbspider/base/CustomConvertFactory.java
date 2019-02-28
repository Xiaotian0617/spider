package com.al.dbspider.base;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class CustomConvertFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type == String.class) {
            return StringResponseConverter.INSTANCE;
        }
        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type == String.class) {
            return StringRequestConverter.INSTANCE;
        }
        return null;
    }

    static final class StringResponseConverter implements Converter<ResponseBody, String> {
        static final StringResponseConverter INSTANCE = new StringResponseConverter();

        @Override
        public String convert(ResponseBody value) throws IOException {
            return value.string();
        }
    }

    static final class StringRequestConverter implements Converter<String, RequestBody> {
        static final StringRequestConverter INSTANCE = new StringRequestConverter();
        private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain; charset=UTF-8");

        private StringRequestConverter() {
        }

        @Override
        public RequestBody convert(String value) throws IOException {
            return RequestBody.create(MEDIA_TYPE, value);
        }
    }
}
