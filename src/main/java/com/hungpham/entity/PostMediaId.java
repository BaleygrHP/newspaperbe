package com.hungpham.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Arrays;

@Getter
@Setter
@Embeddable
public class PostMediaId implements Serializable {

    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private byte[] postId;

    @Column(name = "media_id", columnDefinition = "BINARY(16)")
    private byte[] mediaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostMediaId)) return false;
        PostMediaId that = (PostMediaId) o;
        return Arrays.equals(postId, that.postId) && Arrays.equals(mediaId, that.mediaId);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(postId);
        result = 31 * result + Arrays.hashCode(mediaId);
        return result;
    }
}
