package com.example.be.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiResponseDto {

    @JsonProperty("predictedDisease")
    private String predictedDisease;

    @JsonProperty("probability")
    private Float probability;

    // gs:// 경로가 오는 모든 케이스 수용
    @JsonAlias({
            "gradcamImagePath", "gradcam_path", "gradcamGsPath",
            "gradcam_storage_path", "gradcam_storagePath"
    })
    private String gradcamImagePath;

    // 공개 URL이 오는 모든 케이스 수용
    @JsonAlias({
            "gradcamUrl", "gradcamPublicUrl", "gradcam_download_url",
            "gradcam_image_url", "gradcam_public_url"
    })
    private String gradcamUrl;

    @JsonProperty("top3")
    private List<TopItem> top3;

//    @Getter @Setter
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class TopItem {
//        private String label;
//        private Float prob;
//    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopItem {
        private String label;

        @JsonProperty("prob")
        private Float probability;
    }

    /** 어느 쪽이든 들어온 Grad-CAM 경로를 반환 */
//    public String getGradcamAny() {
//        if (gradcamUrl != null && !gradcamUrl.isEmpty()) return gradcamUrl;
//        return gradcamImagePath;
//    }
//    public String getGradcamImagePath() { return gradcamUrl; }
}