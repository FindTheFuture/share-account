package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.PaymentRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PaymentRecordMapper {

    @Select("SELECT * FROM payment_record WHERE id = #{id}")
    PaymentRecord findById(Long id);

    @Select("SELECT * FROM payment_record WHERE out_trade_no = #{outTradeNo}")
    PaymentRecord findByOutTradeNo(String outTradeNo);

    @Select("SELECT * FROM payment_record WHERE transaction_id = #{transactionId}")
    PaymentRecord findByTransactionId(String transactionId);

    @Select("SELECT * FROM payment_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<PaymentRecord> findByUserId(Long userId);

    @Select("SELECT * FROM payment_record WHERE user_id = #{userId} AND status = #{status} ORDER BY create_time DESC")
    List<PaymentRecord> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Insert("INSERT INTO payment_record (user_id, member_package_id, amount, status, payment_type, out_trade_no, " +
            "create_time, update_time, package_content_snapshot, points_earned) " +
            "VALUES (#{userId}, #{memberPackageId}, #{amount}, #{status}, #{paymentType}, #{outTradeNo}, " +
            "#{createTime}, #{updateTime}, #{packageContentSnapshot}, #{pointsEarned})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentRecord paymentRecord);

    @Update("UPDATE payment_record SET out_trade_no = #{outTradeNo} WHERE id = #{id}")
    int updatePaymentOutTradeNo(PaymentRecord paymentRecord);

    @Update("UPDATE payment_record SET status = #{status}, transaction_id = #{transactionId}, pay_time = #{payTime}, update_time = #{updateTime} WHERE id = #{id}")
    int updatePaymentStatus(PaymentRecord paymentRecord);

    @Update("UPDATE payment_record SET refund_time = #{refundTime}, update_time = #{updateTime}, status = #{status} WHERE id = #{id}")
    int updateRefundStatus(PaymentRecord paymentRecord);

    @Select("SELECT COUNT(1) FROM payment_record WHERE user_id = #{userId} AND status = 1")
    int countSuccessfulPaymentsByUserId(Long userId);

    @Select("SELECT SUM(amount) FROM payment_record WHERE user_id = #{userId} AND status = 1")
    Double getTotalPaymentAmountByUserId(Long userId);
}