package com.micerlabs.controller;

import com.micerlabs.pojo.Result;
import com.micerlabs.service.ImageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
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

    private final int MIN_FREQUENCY_MS=1000;
    private final int MAX_SIZE_PER_DAY=1024*1024*100;//BYTE

    private final int WAIT_TIME_MS=2000;//这是额外的，总时间= MIN_FREQUENCY_MS+WAIT_TIME_MS

    private class Logindata
    {
        public long timeMillis;
        public int day;
        public long size;
        Logindata(long timeMillis,int day,long size)
        {
            this.timeMillis=timeMillis;
            this.day=day;
            this.size=size;
        }
    }
    private Map<String,Logindata> ip_logindata =new HashMap<>();


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
    public Result receiveImage(@RequestParam("file") MultipartFile file, @RequestHeader("hash") String hash,
                               HttpServletRequest request,Authentication authentication) {
        String username=((User)authentication.getPrincipal()).getUsername();
        //String ip=request.getRemoteAddr();
        int flag=acceptRequest(username,file.getSize());
        if(flag==-1)
        {
            return Result.BAD().msg("请求过于频繁，请3秒后再试").build();
        } else if (flag==-2) {
            return Result.BAD().msg("每日上传文件大小超限").build();
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


    /**
     *
     * @param username
     * @param filesize
     * @return 1==成功 -1==过于频繁 -2==每日文件大小超限
     */
    private int acceptRequest(String username,long filesize)
    {
        Long timestamp=System.currentTimeMillis();
        LocalDateTime localDateTime=LocalDateTime.now();
        int day=localDateTime.getDayOfYear();
        if (ip_logindata.containsKey(username))
        {
            Logindata last_d= ip_logindata.get(username);
            long last_t=last_d.timeMillis;
            int last_day=last_d.day;
            long last_size;
            if (day==last_day)
            {
                last_size=last_d.size;
            }else {
                last_size=0;
            }
            long sum_size=last_d.size+filesize;
            if ((timestamp-last_t)<MIN_FREQUENCY_MS)
            {
                ip_logindata.put(username,new Logindata(last_t+WAIT_TIME_MS,day,last_size));
                return -1;

            }else if((sum_size)>MAX_SIZE_PER_DAY)
            {
                return -2;
            }else
            {
                ip_logindata.put(username,new Logindata(timestamp,day,sum_size));
                return 1;

            }
        }else
        {

            if((filesize)>MAX_SIZE_PER_DAY)
            {
                return -2;
            }else
            {
                ip_logindata.put(username,new Logindata(timestamp,day,filesize));
                return 1;

            }
        }
    }
}
