package com.quizai.domain.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForumPostVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;
    private Long questionId;
    private Integer status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private String rejectReason;
    private Integer anonymous;
    private String images;
    private String videos;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String nickname;
    private String avatar;
    private Boolean liked;
    private Boolean favorited;
    private String questionContent;
}
