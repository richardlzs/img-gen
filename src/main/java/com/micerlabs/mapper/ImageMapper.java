package com.micerlabs.mapper;

import com.micerlabs.pojo.Image;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageMapper {
    int insertImage(Image image);

    List<Image> getLatestNImages(int n);

    List<Image> getAllImages();
}
