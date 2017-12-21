package com.studygoal.jisc.Utils.GlideConfig;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

/**
 * Glide Configuration Class
 * <p>
 * Provides the initialisation of the Glide module.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@GlideModule
public class GlideConfiguration extends AppGlideModule {

    /**
     * Registers the custom classes to handle Glide.
     *
     * @param context  app context
     * @param glide    glide object
     * @param registry registry object
     */
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(context));
    }

}