package com.tns.newscrawler.controller;

import com.tns.newscrawler.dto.Tag.TagDto;
import com.tns.newscrawler.dto.Tag.PostTagAttachRequest;
import com.tns.newscrawler.service.PostTag.PostTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/post-tags")
public class PostTagController {

    private final PostTagService postTagService;
    public PostTagController(PostTagService postTagService) { this.postTagService = postTagService; }

    @PostMapping("/attach")
    public ResponseEntity<Void> attach(@RequestBody PostTagAttachRequest req) {
        postTagService.attach(req.getPostId(), req.getTagId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/detach")
    public ResponseEntity<Void> detach(@RequestBody PostTagAttachRequest req) {
        postTagService.detach(req.getPostId(), req.getTagId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<TagDto>> listTagsOfPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postTagService.listTagsOfPost(postId));
    }

    @GetMapping("/tag/{tagId}/posts")
    public ResponseEntity<List<Long>> listPostIdsByTag(@PathVariable Long tagId) {
        return ResponseEntity.ok(postTagService.listPostIdsByTag(tagId));
    }
}
