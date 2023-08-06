package com.github.nicksetzer.daedalus.cast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import com.google.android.exoplayer2.ext.cast.DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;

import java.util.List;

public class UampCastOptionsProvider implements OptionsProvider {

    @NonNull
    @Override
    public CastOptions getCastOptions(@NonNull Context context) {
        return new CastOptions.Builder()
                .setReceiverApplicationId("A12D4273")
                .setCastMediaOptions(
                        new CastMediaOptions.Builder()
                                // We manage the media session and the notifications ourselves.
                                .setMediaSessionEnabled(false)
                                .setNotificationOptions(null)
                                .build()
                )
                .setStopReceiverApplicationWhenEndingSession(true).build();
    }

    @Nullable
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
        return null;
    }
}
