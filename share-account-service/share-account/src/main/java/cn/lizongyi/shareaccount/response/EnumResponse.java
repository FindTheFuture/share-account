package cn.lizongyi.shareaccount.response;

import lombok.Data;

@Data
public class EnumResponse {
    private int id;
    private String description;


    public static EnumResponse create(int id, String description) {
        EnumResponse response = new EnumResponse();
        response.setId(id);
        response.setDescription(description);
        return response;
    }
}