package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.BillCommentMapper;
import cn.lizongyi.shareaccount.entity.BillComment;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.request.CreateBillCommentRequest;
import cn.lizongyi.shareaccount.response.BillCommentResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.BillCommentService;
import cn.lizongyi.shareaccount.services.PictureService;
import cn.lizongyi.shareaccount.enums.PictureTypeEnum;
import cn.lizongyi.shareaccount.util.DateUtil;
import cn.lizongyi.shareaccount.util.HtmlSanitizerUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.lizongyi.shareaccount.dao.BillMapper;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.services.MessageService;
import cn.lizongyi.shareaccount.request.CreateMessageRequest;
import cn.lizongyi.shareaccount.util.JacksonUtils;
import cn.lizongyi.shareaccount.entity.MessageContent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BillCommentServiceImpl implements BillCommentService {

    @Autowired
    private BillCommentMapper billCommentMapper;

    @Autowired
    private BaseHandler baseHandler;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MessageService messageService;

    private final ExecutorService commentMsgExecutor = Executors.newFixedThreadPool(2);

    @Override
    public BillCommentResponse.BillCommentResponseItem getById(Long commentId) {
        if (commentId == null || commentId <= 0) {
            return null;
        }
        BillComment comment = billCommentMapper.findById(commentId);
        if (comment == null) {
            return null;
        }

        if (!baseHandler.canViewBill(baseHandler.getUserId(), comment.getBillId())) {
            log.info("无权限查看账单评论: userId={}, billId={}", baseHandler.getUserId(), comment.getBillId());
            return null;
        }

        BillCommentResponse response = new BillCommentResponse();
        BillCommentResponse.BillCommentResponseItem item = response.new BillCommentResponseItem();
        item.setId(comment.getId());
        item.setBillId(comment.getBillId());
        item.setUserId(comment.getUserId());
        item.setType(comment.getType());
        item.setCreateTime(DateUtil.localDateTimeToString(comment.getCreateTime()));

        try {
            User u = baseHandler.getUserById(comment.getUserId());
            item.setUserName(u != null ? u.getNickName() : "");
        } catch (Exception e) {
            log.error("获取用户昵称失败: userId={}", comment.getUserId(), e);
        }

        if (comment.getType() == 0) {
            // 文本内容（带简单转义）
            item.setContent(HtmlSanitizerUtil.escape(comment.getContent()));
        } else {
            try {
                Picture image = pictureService.findById(Long.parseLong(comment.getContent()));
                if (image != null) {
                    if (image.getStatus() != 1) {
                        item.setImageUrl(image != null ? image.getAddress() : null);
                        item.setContent(null);
                    }
                }
            } catch (Exception e) {
                log.error("获取评论图片失败: commentId={}", item.getId(), e);
            }
        }

        return item;
    }

    @Override
    public BillCommentResponse listByBillId(Long billId, Integer pageNum, Integer pageSize) {
        if (billId == null || billId <= 0) {
            return new BillCommentResponse();
        }
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewBill(currentUserId, billId)) {
            log.info("无权限查看账单评论: userId={}, billId={}", currentUserId, billId);
            return new BillCommentResponse();
        }
        // 默认分页：第1页，每页20
        int page = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
        int size = (pageSize == null || pageSize <= 0) ? 20 : pageSize;
        PageHelper.startPage(page, size);
        // 分页查询评论列表
        List<BillComment> comments = billCommentMapper.findByBillId(billId);
        PageInfo<BillComment> pageInfo = new PageInfo<>(comments);

        BillCommentResponse response = new BillCommentResponse();
        response.setTotal(pageInfo.getTotal());
        List<BillCommentResponse.BillCommentResponseItem> items = new ArrayList<>();

        // 预先按用户去重，缓存用户名和头像URL，避免重复查询与重复生成临时URL
        Set<Long> userIds = new HashSet<>();
        for (BillComment c : comments) {
            if (c.getUserId() != null) {
                userIds.add(c.getUserId());
            }
        }
        Map<Long, String> userNameMap = new HashMap<>();
        Map<Long, String> avatarUrlMap = new HashMap<>();
        for (Long uid : userIds) {
            try {
                User u = baseHandler.getUserById(uid);
                userNameMap.put(uid, u != null ? u.getNickName() : "");
            } catch (Exception e) {
                userNameMap.put(uid, "");
            }
            try {
                List<Picture> pics = pictureService.findByObjectId(uid, PictureTypeEnum.AVATAR_USER.getId());
                if (pics != null && !pics.isEmpty()) {
                    // 最新头像优先
                    pics.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
                    Picture avatar = pics.get(0);
                    if (avatar.getStatus() != 1) {
                        avatarUrlMap.put(uid, avatar != null ? avatar.getAddress() : null);
                    }
                }
            } catch (Exception e) {
                log.error("获取用户头像失败: userId={}", uid, e);
            }
        }

        // 组装评论列表
        for (BillComment c : comments) {
            BillCommentResponse.BillCommentResponseItem r = response.new BillCommentResponseItem();
            r.setId(c.getId());
            r.setBillId(c.getBillId());
            r.setUserId(c.getUserId());
            r.setType(c.getType() == null ? 0 : c.getType());
            r.setCreateTime(DateUtil.localDateTimeToString(c.getCreateTime()));
            r.setUserName(userNameMap.getOrDefault(c.getUserId(), ""));

            if (r.getType() == 0) {
                // 文本内容（带简单转义）
                r.setContent(HtmlSanitizerUtil.escape(c.getContent()));
            } else {
                try {
                    Picture image = pictureService.findById(Long.parseLong(c.getContent()));
                    if (image != null) {
                        if (image.getStatus() != 1) {
                            r.setImageUrl(image != null ? image.getAddress() : null);
                            r.setContent(null);
                        }
                    }
                } catch (Exception e) {
                    log.error("获取评论图片失败: commentId={}", c.getId(), e);
                }
            }
            items.add(r);
        }

        response.setItemList(items);
        response.setAvatarUrlMap(avatarUrlMap);
        // 同步分页元数据：页码、每页大小、总页数
        response.setPageNum(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        response.setTotalPages(pageInfo.getPages());
        return response;
    }

    @Override
    public Boolean save(CreateBillCommentRequest request) {
        if (request == null || request.getBillId() == null || request.getBillId() <= 0) {
            return false;
        }
        Long currentUserId = baseHandler.getUserId();
        if (!baseHandler.canViewBill(currentUserId, request.getBillId())) {
            log.info("无权限添加评论: userId={}, billId={}", currentUserId, request.getBillId());
            return false;
        }
        Integer type = request.getType() == null ? 0 : request.getType();
        String content = request.getContent();
        if (type == 0) {
            if (content == null || content.trim().isEmpty()) {
                return false;
            }
        } else if (type == 1) {
            // 图片：content需为图片ID字符串
            if (content == null || content.trim().isEmpty()) {
                return false;
            }
            try {
                Long.parseLong(content.trim());
            } catch (Exception e) {
                return false;
            }
        }
        // 简单转义，仅对文本类型
        String safeContent = type == 0 ? HtmlSanitizerUtil.escape(content) : content.trim();
        BillComment comment = new BillComment();
        comment.setBillId(request.getBillId());
        comment.setUserId(currentUserId);
        comment.setContent(safeContent);
        comment.setType(type);
        comment.setCreateTime(LocalDateTime.now());
        int rows = billCommentMapper.insert(comment);
        if (rows > 0) {
            commentMsgExecutor.submit(() -> {
                try {
                    Bill bill = billMapper.findById(request.getBillId());
                    if (bill != null && bill.getLedgerId() != null) {
                        Long ledgerId = bill.getLedgerId();

                        // 消息接收人：账单创建人 + 所有有效成员
                        List<Long> targetUserIds = new ArrayList<>();
                        targetUserIds.add(bill.getUserId()); // 账单创建人

                        List<Member> members = memberMapper.findByLedgerId(ledgerId);
                        if (members != null) {
                            for (Member m : members) {
                                if (m != null && m.getStatus() != null && m.getStatus() == 1 && m.getUserId() != null) {
                                    targetUserIds.add(m.getUserId());
                                }
                            }
                        }
                        // 去重并排除当前用户，同时包含账单创建人
                        targetUserIds = targetUserIds.stream().filter(Objects::nonNull).filter(id -> !id.equals(currentUserId)).distinct().collect(Collectors.toList());
                        
                        if (!targetUserIds.isEmpty()) {
                            MessageContent messageContent = new MessageContent();
                            messageContent.setBizType("bill_comment");
                            messageContent.setLedgerId(ledgerId);
                            messageContent.setBillId(request.getBillId());
                            messageContent.setCommentId(comment.getId());
                            messageContent.setFromUserId(currentUserId);

                            String jsonContent = JacksonUtils.objToStr(messageContent, false);

                            CreateMessageRequest msgRequest = new CreateMessageRequest();
                            msgRequest.setTitle("有人评论账单啦");
                            msgRequest.setContent(jsonContent);
                            msgRequest.setType(2); // 业务消息
                            msgRequest.setPriority(0); // 普通
                            msgRequest.setTarget("specific");
                            msgRequest.setUserIds(targetUserIds);

                            messageService.saveMessage(msgRequest, currentUserId);
                        }
                    }
                } catch (Exception e) {
                    log.error("账单评论消息分发失败: billId={}, commentId={}, err={}", request.getBillId(), comment.getId(), e.getMessage());
                }
            });
        }
        return rows > 0;
    }

    @Override
    public Boolean deleteById(Long commentId) {
        if (commentId == null || commentId <= 0) {
            return false;
        }
        Long currentUserId = baseHandler.getUserId();
        BillComment comment = billCommentMapper.findById(commentId);
        if (comment == null) {
            return false;
        }
        boolean canDelete = (comment.getUserId() != null && comment.getUserId().equals(currentUserId));
        if (!canDelete) {
            return false;
        }
        // 若为图片评论，尝试将图片标记为弃用（status=1）
        Integer type = comment.getType() == null ? 0 : comment.getType();
        if (type == 1) {
            String content = comment.getContent();
            if (content != null && !content.trim().isEmpty()) {
                Long imageId = Long.parseLong(content.trim());
                try {
                    if (imageId != null && imageId > 0) {
                        pictureService.updateStatus(imageId);
                    }
                } catch (Exception e) {
                    log.error("删除评论时更改图片状态：弃用，  失败 commentId={}, imageId={}, err={}", commentId, imageId, e.getMessage());
                }
            }
        }
        int rows = billCommentMapper.deleteById(commentId);
        return rows > 0;
    }
}