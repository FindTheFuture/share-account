package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.Picture;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PictureService {
    List<Picture> findAll();
    List<Picture> findByObjectId(Long objectId, Integer type);
    Picture findById(Long id);
    Picture findUserAvatarUrl(Long objectId);
    List<Picture> findByIds(@Param("ids") List<Long> ids);
    List<Picture> findByStatus(Integer status);
    Boolean save(Picture picture);
    Boolean updateStatus(Long id);
    Boolean updateStatus(List<Long> idList);
    Boolean updateObjectId(Long oldObjectId, Long newObjectId);
    Boolean deleteById(Long id);
}