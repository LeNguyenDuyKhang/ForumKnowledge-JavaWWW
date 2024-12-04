package com.fit.iuh.controllers;

import com.fit.iuh.entites.*;
import com.fit.iuh.enums.PostReportState;
import com.fit.iuh.enums.PostState;
import com.fit.iuh.services.*;
import com.fit.iuh.utilities.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostReportService postReportService;
    @Autowired
    private ReactionService reactionService;

    @GetMapping
    public String index() {
        return "views_user/blog";
    }



    @GetMapping("/write_blog_basic")
    public String writeBlog(Model model) {
        Post post = new Post();
        List<Topic> topics = topicService.findAll();
        model.addAttribute("topics", topics);
        model.addAttribute("post", post);
        return "test/write_blog_basic";
    }
    @PostMapping("/save")
    public String savePost(Post post) {
        Date now = new Date();
//        post.setContent("12313123");
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
//        post.setDescription("Khong co");
        post.setUrl("https://viblo.asia/announcements/khao-sat-viblo-nhu-cau-phat-trien-su-nghiep-it-toan-cau-PAoJePaA41j");
        post.setState(PostState.PUBLISHED);
        post.setTotalComments(1);
        post.setTotalUpVote(1);
        post.setTotalDownVote(0);
        post.setTotalShare(0);
        post.setTotalView(10);
//        post.setTopic(topicService.findById(1));
        post.setAuthor(userService.findById(1));
        
        postService.save(post);
        return "redirect:/";
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("keyword") String keyword) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", HttpStatus.OK.value());

        response.put("data", postService.search(keyword));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        User currentUser = userService.findUserByEmail(SpringContext.getCurrentUserEmail());
        model.addAttribute("currentUser", currentUser);
        
        model.addAttribute("idPost", id.trim());

        Post post = postService.findByIdAndUrl(id.trim());
        if (post == null){
            return "redirect:/";
        }

        boolean isFollowing = false;
        boolean isOwner = false;

        if (currentUser != null){
            Reaction reaction = reactionService.hasUserVoted(currentUser.getUserId(), post.getPostId());

            isFollowing = userService.isFollowing(currentUser.getUserId(), post.getAuthor().getUserId()).size() > 0;
            isOwner = post.getAuthor().getUserId() == currentUser.getUserId();

            if (reaction != null){
                model.addAttribute("reaction", reaction.getType().toString());
            } else {
                model.addAttribute("reaction", "");
            }
        }

        model.addAttribute("post", post);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isOwner", isOwner);
        return "views_user/view-post";
    }
  
    @GetMapping("/show_detail/{postId}")
    public String showDetail(Model model, @PathVariable("postId") int id) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        return "test/show_detail_test";
    }

    @GetMapping("/report/{postId}")
    public String report(Model model, @PathVariable("postId") int postId) {
        Post post = postService.findById(postId);
        PostReport report = new PostReport();
        model.addAttribute("post", post);
        model.addAttribute("report", report);
        return "test/report";
    }
    @PostMapping("/reported/{postId}")
    public String reported(PostReport postReport , @PathVariable("postId") int postId) {
        Date now = new Date();
        postReport.setState(PostReportState.PROCESSING);
        postReport.setInspector(userService.findById(1));
        postReport.setReporter(userService.findById(1));
        postReport.setPost(postService.findById(postId));
        postReportService.saveOrUpdatePostReport(postReport);
        return "redirect:/posts/show";

    }
}
