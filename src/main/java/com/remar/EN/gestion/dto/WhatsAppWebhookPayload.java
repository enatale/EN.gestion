package com.remar.EN.gestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    private String object;
    private List<Entry> entry;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private List<Change> changes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private Value value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        private List<Message> messages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String from;
        private String type;
        private ImageMedia image;
        private DocumentMedia document;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageMedia {
        private String id;
        @JsonProperty("mime_type")
        private String mimeType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentMedia {
        private String id;
        @JsonProperty("mime_type")
        private String mimeType;
        private String filename;
    }
}
