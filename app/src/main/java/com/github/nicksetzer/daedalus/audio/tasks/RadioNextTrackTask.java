package com.github.nicksetzer.daedalus.audio.tasks;

import com.github.nicksetzer.daedalus.Log;
import com.github.nicksetzer.daedalus.api.YueApi;
import com.github.nicksetzer.daedalus.audio.AudioService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class RadioNextTrackTask implements Runnable {

    AudioService m_service;
    String m_token;
    String m_station;

    public RadioNextTrackTask(AudioService service, String token, String station) {
        m_service = service;
        m_token = token;
        m_station = station;

    }

    public void run() {

        try {

            JSONObject track = YueApi.radioStationNextTrack(m_token, m_station);

            if (track == null) {
                Log.error("failed to get next track");
                return;
            }

            String source = track.optString("source", "");
            String sid = track.optString("sid", "");
            JSONObject stream = track.optJSONObject("stream");
            String url = (stream!=null)?stream.optString("url", ""):"";

            if (url.isEmpty() || url.equals("null")) {
                if (source.equals("library")) {
                    Log.warn("building library stream url");
                    // TODO: query database for possible local match
                    url = YueApi.librarySongAudioUrl(m_token, sid);
                } else {
                    Log.warn("empty url for unknown source: `" + source + "`");
                }
            }

            Log.warn("using stream url: `" + url + "`");

            if (url.isEmpty() || url.equals("null")) {
                Log.warn("failed to determine track stream");
            } else {
                m_service.m_manager.playRadioUrl(url);
                m_service.sendEvent("ontrackchanged", track.toString());
            }

        } catch (IOException e) {
            Log.error("failed to get next song", e);
        }
    }


}
