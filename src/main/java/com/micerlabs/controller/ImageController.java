package com.micerlabs.controller;

import com.micerlabs.pojo.Result;
import com.micerlabs.service.ImageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
public class ImageController {
    @Resource
    private ImageService imageService;

    @GetMapping("/list/last")
    public Result getImgList(@RequestParam(value = "n", defaultValue = "10") int n) {
        return Result.OK().data(imageService.getLatestNImages(n < 0 || n > 10 ? 10 : n)).build();
    }

    @GetMapping("/list/page")
    public Result getImgListWithPage(@RequestParam(value = "offset", defaultValue = "1") int offset,
                                     @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return Result.OK().data(imageService.getImagesWithPage(offset, limit)).build();
    }

    @PostMapping("/image")
    public Result receiveImage(@RequestParam("file") MultipartFile file, @RequestHeader("hash") String hash) {
        int receive = imageService.receiveImage(file,hash);
        if (receive == 0) {
            return Result.BAD().build();
        }
        return Result.OK().build();
    }

    @GetMapping("/loggedIn")
    public String isLoggedIn() {
        return "1";
    }
}
