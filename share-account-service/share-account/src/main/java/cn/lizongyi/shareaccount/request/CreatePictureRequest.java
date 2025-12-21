package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreatePictureRequest {
    private Long id;
    private String name;
    private String path;
    private int type;
    private String address;
}