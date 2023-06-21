package com.micerlabs.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.micerlabs.mapper.ImageMapper;
import com.micerlabs.pojo.Image;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class ImageService {

    @Value("${path.local}")
    private String localPath;

    @Value("${path.remote}")
    private String remotePath;

    @Resource
    private ImageMapper imageMapper;


    /**
     * 接收图片存入本地文件系统，并将元数据保存至数据库
     * @param file 接收图片文件
     * @return success: 1; fail: 0
     */
    public int receiveImage(MultipartFile file,String hash) {
        Random r=new Random();
        String imgId = String.format("%08d",r.nextInt(100000000))+Sid.next();//解决热点问题
        String dir = createDirIfNotExist(imgId);//解决热点问题
        //检查hash
        try (InputStream ins = file.getInputStream()) {

            String hash_now = get_hash_SHA1(ins);

            if (!Objects.equals(hash_now, hash)) {
                System.out.println("hash不一致！！拒绝接收");
                return 0;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }


        if (dir == null) {
            // 目录创建失败
            return 0;
        }
        String fileName = imgId + ".jpg";
        long timestamp = Long.parseLong(Objects.requireNonNull(file.getOriginalFilename()).substring(0, file.getOriginalFilename().lastIndexOf(".")));
        // 存入文件系统
        File targetFile = new File(dir, fileName);
        try {
            decompressFile(file.getInputStream(),targetFile);
            log.info("======>>>>图片存储成功");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("======>>>>图片存储失败");
            return 0;
        }
        // 存入Mysql
        Image image = new Image(imgId, new Date(timestamp), dir + fileName);
        return imageMapper.insertImage(image);
    }

    /**
     * 获取最近n张图片
     * @param n 图片数量
     * @return 图片列表
     */
    public List<Image> getLatestNImages(int n) {
        List<Image> list = imageMapper.getLatestNImages(n);
        list = replaceRemotePath(list);
        return list;
    }

    /**
     * 获取图片列表 (前端渲染分页使用)
     * @param offset pageNum
     * @param limit pageSize
     * @return map {list -> 图片列表, count -> 图片总数量}
     */
    public Map<String, Object> getImagesWithPage(int offset, int limit) {
        Map<String, Object> res = new HashMap<>();
        PageHelper.startPage(offset, limit);
        List<Image> list = imageMapper.getAllImages();
        PageInfo<Image> info = new PageInfo<>(list);
        list = replaceRemotePath(list);
        long count = info.getTotal();
        res.put("list", list);
        res.put("count", count);
        return res;
    }

    /**
     * 替换图片路径
     * @param list 从数据库中获取到的imageList image.path -> localDir
     * @return 返回给前端的imageList image.path -> remotePath
     */
    private List<Image> replaceRemotePath(List<Image> list) {
        return list.stream().peek(image -> image.setPath(image.getPath().replace(localPath, remotePath).replace("\\", "/"))).collect(Collectors.toList());
    }

    /**
     * 若指定目录不存在则创建目录
     * @param id 图片id
     * @return 图片存放路径
     */
    public String createDirIfNotExist(String id) {
        String dir = localPath + id.substring(0, 4) + File.separator + id.substring(4, 8) + File.separator;
        File file = new File(dir);
        if (!file.exists() && !file.mkdirs()) {
            // 目录创建失败
            log.error("======>>>>目录创建失败");
            return null;
        }
        return dir;
    }

    /**
     * get_hash
     * @param inputStream 输入流
     * @return String:SHA-1(formax:0x) or null
     */
    public String get_hash_SHA1(InputStream inputStream)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            byte[] buffer = new byte[8192]; // 缓冲区大小
            while (dis.read(buffer) != -1) {
                // 读取文件内容以更新哈希值
            }
            dis.close();

            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b)); // 转换为十六进制表示
            }
            String hash = sb.toString();

            System.out.println("SHA-1 Hash value: " + hash);
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 解压文件
     * @param inputStream 输入流
     * @param save_file 存储文档的File对象
     * @return success:1 fail:0
     */
    public static int decompressFile(InputStream inputStream,File save_file) throws IOException {
        GZIPInputStream gzipIS = null;
        FileOutputStream fos = null;

        try {

            gzipIS = new GZIPInputStream(inputStream);
            fos = new FileOutputStream(save_file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = gzipIS.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            //System.out.println("文件解压成功！");

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } finally {
            // 关闭资源
            if (gzipIS != null) {
                try {
                    gzipIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return 1;
    }


}
