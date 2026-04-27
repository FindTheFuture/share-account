package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.response.ContactResponse;
import cn.lizongyi.shareaccount.response.FriendRequestResponse;

import java.util.List;

public interface ContactService {

    List<ContactResponse> getContactList(Long userId);

    List<FriendRequestResponse> getPendingRequests(Long userId);

    int getPendingRequestCount(Long userId);

    AddContactResult addContact(Long userId, String phone);

    boolean acceptContact(Long contactId, Long userId);

    boolean rejectContact(Long contactId, Long userId);

    Long findUserIdByPhone(String phone);

    class AddContactResult {
        private boolean success;
        private String errorMessage;

        public AddContactResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
