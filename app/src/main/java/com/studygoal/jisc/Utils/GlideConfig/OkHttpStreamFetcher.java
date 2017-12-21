package com.studygoal.jisc.Utils.GlideConfig;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.util.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Glide Ok Http Stream Fetcher Class
 * <p>
 * Provides the http stream fetching if the connection is ok.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class OkHttpStreamFetcher implements DataFetcher<InputStream> {

    private final OkHttpClient client;
    private final GlideUrl url;
    private InputStream stream;
    private ResponseBody responseBody;

    public OkHttpStreamFetcher(OkHttpClient client, GlideUrl url) {
        this.client = client;
        this.url = url;
    }

    /**
     * Loads the data from the stream.
     *
     * @param priority priority of the call
     * @param callback callback after it finished
     */
    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url.toStringUrl());

            for (Map.Entry<String, String> headerEntry : url.getHeaders().entrySet()) {
                String key = headerEntry.getKey();
                requestBuilder.addHeader(key, headerEntry.getValue());
            }

            Request request = requestBuilder.build();

            Response response = client.newCall(request).execute();
            responseBody = response.body();
            if (!response.isSuccessful()) {
                throw new IOException("Request failed with code: " + response.code());
            }

            long contentLength = responseBody.contentLength();
            stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
            callback.onDataReady(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans the stream.
     */
    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignored
            }
        }
        if (responseBody != null) {
            responseBody.close();
        }
    }

    /**
     * Overridden to do nothing on cancel.
     */
    @Override
    public void cancel() {
        // do nothing
    }

    /**
     * Overridden to return null.
     *
     * @return null
     */
    @Override
    public Class<InputStream> getDataClass() {
        return null;
    }

    /**
     * Gets remote data source.
     *
     * @return remote data source object
     */
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }

}