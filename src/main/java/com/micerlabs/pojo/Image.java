package com.micerlabs.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Image {

    @JsonIgnore
    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date time;

    private String path;

    @JsonIgnore
    private int active;

    public Image(String _id, Date _time, String _path) {
        id = _id;
        time = _time;
        path = _path;
        active = 1;
    }
}
