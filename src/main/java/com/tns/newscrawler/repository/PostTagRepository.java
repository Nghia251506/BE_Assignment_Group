package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPost_Id(Long postId);
    List<PostTag> findByTag_Id(Long tagId);
    Optional<PostTag> findByPost_IdAndTag_Id(Long postId, Long tagId);
    void deleteByPost_IdAndTag_Id(Long postId, Long tagId);
    List<PostTag> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}
