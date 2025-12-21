package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.PictureMapper;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.services.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class PictureServiceImpl implements PictureService {
    @Autowired
    private PictureMapper pictureMapper;

    @Override
    public List<Picture> findAll() {
        return pictureMapper.findAll();
    }

    @Override
    public List<Picture> findByObjectId(Long objectId, Integer type) {
        return pictureMapper.findByObjectIdAndType(objectId, type);
    }

    @Override
    public Picture findById(Long id) {
        return pictureMapper.findById(id);
    }

    @Override
    public List<Picture> findByIds(List<Long> ids) {
        return pictureMapper.findByIds(ids);
    }

    @Override
    public List<Picture> findByStatus(Integer status){
        return pictureMapper.findByStatus(status);
    }

    @Override
    public Boolean save(Picture picture) {
        if (picture.getId() == null) {
            return pictureMapper.insert(picture) > 0;
        } else {
            return pictureMapper.update(picture) > 0;
        }
    }


    @Override
    public Boolean updateStatus(Long id){
        if(id == null || id <= 0L){
            return false;
        }
        Picture picture = pictureMapper.findById(id);

        // 这里限制只能从 0 -》 1
        if(picture == null || picture.getStatus() == 1){
            return false;
        }

        return pictureMapper.updateStatus(id, 1) > 0;
    }

    @Override
    public Boolean updateStatus(List<Long> idList) {
        if(CollectionUtils.isEmpty(idList)){
            return false;
        }
        for (Long id : idList) {
            Boolean success = updateStatus(id);
            if(!success){
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean updateObjectId(Long oldObjectId, Long newObjectId) {
        return pictureMapper.updateObjectId(oldObjectId, newObjectId) > 0;
    }


    @Override
    public Boolean deleteById(Long id) {
        return pictureMapper.deleteById(id) > 0;
    }

}