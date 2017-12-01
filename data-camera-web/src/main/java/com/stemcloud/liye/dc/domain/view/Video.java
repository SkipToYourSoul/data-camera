package com.stemcloud.liye.dc.domain.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  video data
 *  http://docs.videojs.com/tutorial-options.html
 * @author liye on 2017/12/1
 */
public class Video {
    private VideoConfig option;
    private long recorderId;

    public VideoConfig getOption() {
        return option;
    }

    public void setOption(String poster, String src) {
        this.option = new VideoConfig(poster, src);
    }

    public long getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(long recorderId) {
        this.recorderId = recorderId;
    }

    private class VideoConfig {
        private String poster;
        private List<Map> sources;
        private List<String> techOrder;
        private String preload = "auto";
        private boolean controls = true;

        private VideoConfig(String poster, final String src){
            if (poster != null) {
                this.poster = poster;
            }

            // -- init sources
            if (src != null) {
                Map<String, String> map = new HashMap<String, String>() {{
                    put("src", src);
                    int length = src.split(".").length;
                    if (length == 0) {
                        put("type", "video/mp4");
                    } else {
                        String suffix = src.split(".")[length - 1];
                        put("type", "video/" + suffix);
                    }
                }};
                List<Map> sources = new ArrayList<Map>();
                sources.add(map);
                this.sources = sources;
            }

            // -- init techOrder
            List<String> techOrder = new ArrayList<String>();
            techOrder.add("html5");
            techOrder.add("flash");
            this.techOrder = techOrder;
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }

        public List<Map> getSources() {
            return sources;
        }

        public void setSources(List<Map> sources) {
            this.sources = sources;
        }

        public List<String> getTechOrder() {
            return techOrder;
        }

        public void setTechOrder(List<String> techOrder) {
            this.techOrder = techOrder;
        }

        public String getPreload() {
            return preload;
        }

        public void setPreload(String preload) {
            this.preload = preload;
        }

        public boolean isControls() {
            return controls;
        }

        public void setControls(boolean controls) {
            this.controls = controls;
        }
    }
}
