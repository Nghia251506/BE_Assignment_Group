package com.tns.newscrawler.service.PostTag;

import com.tns.newscrawler.dto.Tag.TagDto;
import com.tns.newscrawler.entity.Post;
import com.tns.newscrawler.entity.PostTag;
import com.tns.newscrawler.entity.Tag;
import com.tns.newscrawler.mapper.Tag.TagMapper;
import com.tns.newscrawler.repository.PostRepository;
import com.tns.newscrawler.repository.PostTagRepository;
import com.tns.newscrawler.repository.TagRepository;
import com.tns.newscrawler.service.PostTag.PostTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostTagServiceImpl implements PostTagService {

    private final PostRepository postRepo;
    private final TagRepository tagRepo;
    private final PostTagRepository postTagRepo;

    public PostTagServiceImpl(PostRepository postRepo, TagRepository tagRepo, PostTagRepository postTagRepo) {
        this.postRepo = postRepo;
        this.tagRepo = tagRepo;
        this.postTagRepo = postTagRepo;
    }

    @Override
    public void attach(Long postId, Long tagId) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Tag tag = tagRepo.findById(tagId).orElseThrow(() -> new RuntimeException("Tag not found"));
        boolean exists = postTagRepo.findByPost_IdAndTag_Id(postId, tagId).isPresent();
        if (!exists) {
            postTagRepo.save(PostTag.builder().post(post).tag(tag).build());
        }
    }

    @Override
    public void detach(Long postId, Long tagId) {
        postTagRepo.deleteByPost_IdAndTag_Id(postId, tagId);
    }

    @Override
    public List<TagDto> listTagsOfPost(Long postId) {
        return postTagRepo.findByPost_Id(postId).stream()
                .map(pt -> TagMapper.toDto(pt.getTag()))
                .toList();
    }

    @Override
    public List<Long> listPostIdsByTag(Long tagId) {
        return postTagRepo.findByTag_Id(tagId).stream()
                .map(pt -> pt.getPost().getId())
                .toList();
    }
}
