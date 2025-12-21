package com.hungpham.dtos;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FrontPageDto {
    private PostDto featured;
    private List<PostDto> latest;
    private List<PostDto> curated;
    private List<PostDto> editorialBlock;
    private List<PostDto> diaryBlock;
    private List<PostDto> notesBlock;
}
