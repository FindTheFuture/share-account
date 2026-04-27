package cn.lizongyi.shareaccount.services;

public interface SmsService {

    void sendSmsCode(String phone, String ipAddress);

    boolean verifySmsCode(String phone, String code, String type);
}