package com.studygoal.jisc.Utils.GlideConfig;

import android.content.Context;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * Glide Ok Http Url Uploader Class
 * <p>
 * Provides the http url uploader if the connection is ok.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class OkHttpUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private final OkHttpClient mClient;

    public OkHttpUrlLoader(OkHttpClient client) {
        mClient = client;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(GlideUrl glideUrl, int width, int height, Options options) {
        return new LoadData<>(glideUrl, new OkHttpStreamFetcher(mClient, glideUrl));
    }

    @Override
    public boolean handles(GlideUrl glideUrl) {
        return true;
    }

    /**
     * Model Loader Factory for the loading.
     */
    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private static volatile OkHttpClient internalClient;
        private OkHttpClient client;

        /**
         * Gets the internal client
         *
         * @param appContext app context.
         * @return client object
         */
        private static OkHttpClient getInternalClient(final Context appContext) {
            if (internalClient == null) {
                synchronized (Factory.class) {
                    if (internalClient == null) {
                        internalClient = UnsafeOkHttpClient.getUnsafeOkHttpClient(appContext);
                    }
                }
            }
            return internalClient;
        }

        /**
         * Constructor for a new Factory that runs requests using a static singleton client.
         */
        public Factory(final Context appContext) {
            this(getInternalClient(appContext));
        }

        /**
         * Constructor for a new Factory that runs requests using given client.
         */
        public Factory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public ModelLoader<GlideUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return new OkHttpUrlLoader(client);
        }

        /**
         * Overriden to do nothing.
         */
        @Override
        public void teardown() {
            // Do nothing, this instance doesn't own the client.
        }
    }
}