package io.github.froger.instamaterial.models;

public class QwantImage {
    private String media;

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        if (!media.contains("https")) {media = media.replace("http","https");}
        this.media = media;
    }
}
