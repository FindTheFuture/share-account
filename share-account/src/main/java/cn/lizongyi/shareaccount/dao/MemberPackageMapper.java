package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.MemberPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemberPackageMapper {

    @Select("SELECT * FROM member_package WHERE id = #{id}")
    MemberPackage findById(Long id);

    @Select("SELECT * FROM member_package WHERE status = 1 ORDER BY sort ASC")
    List<MemberPackage> findActivePackages();

    @Select("SELECT * FROM member_package WHERE type = #{type} AND status = 1 ORDER BY sort ASC")
    List<MemberPackage> findActivePackagesByType(Integer type);

    @Select("SELECT * FROM member_package ORDER BY sort ASC")
    List<MemberPackage> findAllPackages();

    @Select("SELECT COUNT(1) FROM member_package WHERE type = #{type} AND status = 1")
    int countActivePackagesByType(Integer type);
}