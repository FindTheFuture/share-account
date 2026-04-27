package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.ContactMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.entity.Contact;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.response.ContactResponse;
import cn.lizongyi.shareaccount.response.FriendRequestResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ContactService;
import cn.lizongyi.shareaccount.services.PictureService;
import cn.lizongyi.shareaccount.util.PhoneUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactMapper contactMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private BaseHandler baseHandler;

    @Override
    public List<ContactResponse> getContactList(Long userId) {
        List<Contact> contacts = contactMapper.findByUserIdAndStatus(userId, Contact.STATUS_ADDED);
        if (CollectionUtils.isEmpty(contacts)) {
            return new ArrayList<>();
        }

        List<Long> friendIds = contacts.stream()
                .map(Contact::getFriendId)
                .collect(Collectors.toList());

        List<User> friends = userMapper.findByIds(friendIds);
        Map<Long, User> userMap = friends.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ContactResponse> responses = new ArrayList<>();
        for (Contact contact : contacts) {
            User friend = userMap.get(contact.getFriendId());
            if (friend == null) {
                continue;
            }

            ContactResponse response = new ContactResponse();
            response.setContactId(contact.getId());
            response.setUserId(friend.getId());
            response.setNickName(friend.getNickName());
            response.setPhone(PhoneUtil.maskPhoneNumber(friend.getPhone()));
            response.setAvatarUrl(getAvatarUrl(friend.getId()));

            responses.add(response);
        }

        responses.sort((a, b) -> {
            String nickA = a.getNickName() != null ? a.getNickName() : "";
            String nickB = b.getNickName() != null ? b.getNickName() : "";
            return nickA.compareToIgnoreCase(nickB);
        });

        return responses;
    }

    @Override
    public List<FriendRequestResponse> getPendingRequests(Long userId) {
        List<Contact> requests = contactMapper.findPendingRequests(userId);
        if (CollectionUtils.isEmpty(requests)) {
            return new ArrayList<>();
        }

        List<Long> requesterIds = requests.stream()
                .map(Contact::getUserId)
                .collect(Collectors.toList());

        List<User> users = userMapper.findByIds(requesterIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<FriendRequestResponse> responses = new ArrayList<>();
        for (Contact contact : requests) {
            User requester = userMap.get(contact.getUserId());
            if (requester == null) {
                continue;
            }

            FriendRequestResponse response = new FriendRequestResponse();
            response.setContactId(contact.getId());
            response.setUserId(requester.getId());
            response.setNickName(requester.getNickName());
            response.setPhone(PhoneUtil.maskPhoneNumber(requester.getPhone()));
            response.setAvatarUrl(getAvatarUrl(requester.getId()));
            response.setCreateTime(contact.getCreateTime() != null ? contact.getCreateTime().toString() : "");

            responses.add(response);
        }

        return responses;
    }

    @Override
    public int getPendingRequestCount(Long userId) {
        return contactMapper.countPendingRequests(userId);
    }

    @Override
    public AddContactResult addContact(Long userId, String phone) {
        Long friendId = findUserIdByPhone(phone);
        if (friendId == null) {
            log.info("用户不存在: phone={}", phone);
            return new AddContactResult(false, "用户不存在");
        }

        if (friendId.equals(userId)) {
            log.info("不能添加自己为联系人: userId={}", userId);
            return new AddContactResult(false, "不能添加自己");
        }

        List<Contact> existing = contactMapper.findRelation(userId, friendId);
        for (Contact c : existing) {
            if (c.getStatus() == Contact.STATUS_PENDING) {
                log.info("已经存在待同意的好友请求: userId={}, friendId={}", userId, friendId);
                return new AddContactResult(false, "已发送过好友请求，等待对方同意");
            }
            if (c.getStatus() == Contact.STATUS_ADDED) {
                log.info("已经是好友了: userId={}, friendId={}", userId, friendId);
                return new AddContactResult(false, "已经是好友了");
            }
        }

        Contact contact = new Contact();
        contact.setUserId(userId);
        contact.setFriendId(friendId);
        contact.setStatus(Contact.STATUS_PENDING);
        contactMapper.insert(contact);

        log.info("发送好友请求成功: userId={}, friendId={}", userId, friendId);
        return new AddContactResult(true, null);
    }

    @Override
    public boolean acceptContact(Long contactId, Long userId) {
        Contact contact = contactMapper.findByIdAndStatus(contactId, Contact.STATUS_PENDING);

        if (contact == null) {
            log.info("不存在待同意的好友请求: contactId={}, userId={}", contactId, userId);
            return false;
        }

        contact.setStatus(Contact.STATUS_ADDED);
        boolean success = contactMapper.updateStatus(contactId, Contact.STATUS_ADDED) > 0;

        if (!success) {
            log.info("同意好友请求失败: contactId={}", contactId);
            return false;
        }

        Contact reverse = new Contact();
        reverse.setUserId(contact.getFriendId());
        reverse.setFriendId(contact.getUserId());
        reverse.setStatus(Contact.STATUS_ADDED);
        success = success && contactMapper.insert(reverse) > 0;

        if (!success) {
            log.info("同意好友请求失败: contactId={}", contactId);
            return false;
        }

        log.info("同意好友请求: contactId={}, userId={}", contactId, userId);
        return true;
    }

    @Override
    public boolean rejectContact(Long contactId, Long userId) {
        Contact contact = contactMapper.findByUserIdAndStatus(userId, Contact.STATUS_PENDING)
                .stream()
                .filter(c -> c.getId().equals(contactId))
                .findFirst()
                .orElse(null);

        if (contact == null) {
            log.info("不存在待拒绝的好友请求: contactId={}, userId={}", contactId, userId);
            return false;
        }

        contact.setStatus(Contact.STATUS_REJECTED);
        contactMapper.updateStatus(contactId, Contact.STATUS_REJECTED);

        log.info("拒绝好友请求: contactId={}, userId={}", contactId, userId);
        return true;
    }

    @Override
    public Long findUserIdByPhone(String phone) {
        List<User> users = userMapper.findByPhone(phone);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        return users.get(0).getId();
    }

    private String getAvatarUrl(Long userId) {
        Picture picture = pictureService.findUserAvatarUrl(userId);
        if(picture == null){
            log.info("根据userId： {}  没有查询到图片", userId);
            return baseHandler.getDefaultUserAvatar(null);
        }
        baseHandler.fillPicPresignUrl(picture);
        return picture.getAddress();
    }
}
