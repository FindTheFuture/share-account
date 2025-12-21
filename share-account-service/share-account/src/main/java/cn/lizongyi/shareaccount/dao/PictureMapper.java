package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.Picture;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PictureMapper {
    @Select("SELECT * FROM picture")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "path", column = "path"),
            @Result(property = "type", column = "type"),
            @Result(property = "address", column = "address"),
            @Result(property = "status", column = "status"),
            @Result(property = "objectId", column = "objectId"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Picture> findAll();


    @Select("SELECT * FROM picture where objectId=#{objectId} and status = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "path", column = "path"),
            @Result(property = "type", column = "type"),
            @Result(property = "address", column = "address"),
            @Result(property = "status", column = "status"),
            @Result(property = "objectId", column = "objectId"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Picture> findByObjectId(Long objectId);


    @Select("SELECT * FROM picture where objectId=#{objectId} and status = 0 and type = #{type}")
    List<Picture> findByObjectIdAndType(Long objectId, Integer type);



    @Select("SELECT * FROM picture WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "path", column = "path"),
            @Result(property = "type", column = "type"),
            @Result(property = "address", column = "address"),
            @Result(property = "status", column = "status"),
            @Result(property = "objectId", column = "objectId"),
            @Result(property = "createTime", column = "create_time")
    })
    Picture findById(Long id);

    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "path", column = "path"),
            @Result(property = "type", column = "type"),
            @Result(property = "address", column = "address"),
            @Result(property = "status", column = "status"),
            @Result(property = "objectId", column = "objectId"),
            @Result(property = "createTime", column = "create_time")
    })
    List<Picture> findByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM picture WHERE status = #{status}")
    List<Picture> findByStatus(@Param("status") Integer status);

    @Insert("INSERT INTO picture (name, path, type, address, status, objectId, create_time) VALUES (#{name}, #{path}, #{type}, #{address}, #{status}, #{objectId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Picture picture);

    @Update("UPDATE picture SET name=#{name}, path=#{path}, type = #{type}, address = #{address}, status = #{status}, objectId=#{objectId} WHERE id = #{id}")
    int update(Picture picture);

    @Update("UPDATE picture SET status = #{status} WHERE id = #{id}")
    int updateStatus(Long id, Integer status);

    @Update("UPDATE picture SET objectId = #{newObjectId} WHERE objectId = #{oldObjectId}")
    int updateObjectId(Long oldObjectId, Long newObjectId);

    @Delete("DELETE FROM picture WHERE id = #{id}")
    int deleteById(Long id);
}