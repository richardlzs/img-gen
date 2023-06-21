package com.micerlabs.controller;

import com.micerlabs.pojo.Result;
import com.micerlabs.service.ImageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ImageController {
    @Resource
    private ImageService imageService;

    private Map<String,Long> ip_logintime=new HashMap<>();



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
    public Result receiveImage(@RequestParam("file") MultipartFile file, @RequestHeader("hash") String hash, HttpServletRequest request) {
        String ip=request.getRemoteAddr();
        if(!acceptRequest(ip))
        {
            return Result.BAD().msg("请求过于频繁，请3秒后再试").build();
        }
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

    private boolean acceptRequest(String ip)
    {
        Long timestamp=System.currentTimeMillis();
        if (ip_logintime.containsKey(ip))
        {
            Long last_t=ip_logintime.get(ip);
            if ((timestamp-last_t)<1000)
            {
                ip_logintime.put(ip,timestamp+2000);
                return false;
            }else
            {
                ip_logintime.put(ip,timestamp);
                return true;
            }
        }else
        {
            ip_logintime.put(ip,timestamp);
            return true;
        }
    }
}
