package com.synkork.backend.modules.admin.rooms.email;

import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.room.RoomEntity;
import com.synkork.backend.modules.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AdminRoomEmailService {

    @Autowired
    private EmailService emailService;

    @Async
    public void sendRoomCreatedEmail(RoomEntity room, UserEntity owner) {
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        String roomName = room.getName() != null ? room.getName() : "Direct Message";
        String roomDesc = room.getDescription() != null ? room.getDescription() : "Không có mô tả";
        String subject = "[Synkork] Phòng mới của bạn đã được tạo";
        
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">🎉 Phòng mới đã được tạo!</h2>
                    <p style="color: #374151;">
                        Xin chào <strong>%s</strong>,<br/>
                        Quản trị viên hệ thống đã tạo một phòng mới cho bạn trên Synkork.
                    </p>
                    <div style="margin: 24px 0; padding: 16px; background: #f0fdf4;
                                border-left: 4px solid #22c55e; border-radius: 8px;">
                        <p style="margin: 0; color: #166534; line-height: 1.6;">
                            📂 <strong>Tên phòng:</strong> %s<br/>
                            📝 <strong>Mô tả:</strong> %s<br/>
                            👤 <strong>Vai trò:</strong> Chủ phòng (Owner)
                        </p>
                    </div>
                    <p style="color: #374151;">
                        Bạn có thể đăng nhập vào hệ thống để bắt đầu thiết lập và cộng tác cùng các thành viên khác ngay bây giờ.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(owner.getUsername(), roomName, roomDesc);

        emailService.send(owner.getEmail(), subject, body);
    }

    @Async
    public void sendRoomOwnerTransferredFromEmail(
            String oldOwnerEmail,
            String oldOwnerUsername,
            String roomName,
            String newOwnerUsername,
            String newOwnerEmail
    ) {
        if (oldOwnerEmail == null || oldOwnerEmail.isBlank()) {
            return;
        }

        String safeRoomName = roomName != null ? roomName : "Direct Message";
        String subject = "[Synkork] Thay đổi quyền sở hữu phòng " + safeRoomName;

        String body = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                        padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                <h2 style="color: #b45309;">🔄 Thay đổi quyền sở hữu phòng</h2>
                <p style="color: #374151;">
                    Xin chào <strong>%s</strong>,<br/>
                    Chúng tôi muốn thông báo rằng quyền sở hữu của phòng <strong>%s</strong> đã được chuyển giao cho người dùng khác bởi quản trị viên.
                </p>
                <div style="margin: 24px 0; padding: 16px; background: #fff7ed;
                            border-left: 4px solid #f97316; border-radius: 8px;">
                    <p style="margin: 0; color: #92400e; line-height: 1.6;">
                        📂 <strong>Phòng:</strong> %s<br/>
                        👤 <strong>Chủ sở hữu mới:</strong> %s (%s)<br/>
                        ⚠️ <strong>Trạng thái:</strong> Bạn không còn là chủ sở hữu của phòng này.
                    </p>
                </div>
                <p style="color: #374151;">
                    Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với quản trị viên hoặc đội ngũ hỗ trợ của Synkork.
                </p>
                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                    Đây là email tự động từ Synkork — vui lòng không reply.
                </p>
            </div>
            """.formatted(oldOwnerUsername, safeRoomName, safeRoomName, newOwnerUsername, newOwnerEmail);

        emailService.send(oldOwnerEmail, subject, body);
    }

    @Async
    public void sendRoomOwnerTransferredToEmail(UserEntity newOwner, RoomEntity room, UserEntity oldOwner) {
        if (newOwner == null || newOwner.getEmail() == null || newOwner.getEmail().isBlank()) {
            return;
        }

        String roomName = room.getName() != null ? room.getName() : "Direct Message";
        String subject = "[Synkork] Bạn đã trở thành chủ sở hữu mới của phòng " + roomName;
        String oldOwnerName = oldOwner != null ? oldOwner.getUsername() : "Hệ thống";
        String oldOwnerEmail = oldOwner != null ? oldOwner.getEmail() : "admin@synkork.com";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">🎉 Nhận quyền sở hữu phòng mới</h2>
                    <p style="color: #374151;">
                        Xin chào <strong>%s</strong>,<br/>
                        Quản trị viên đã chuyển quyền sở hữu phòng <strong>%s</strong> sang cho bạn.
                    </p>
                    <div style="margin: 24px 0; padding: 16px; background: #f0fdf4;
                                border-left: 4px solid #22c55e; border-radius: 8px;">
                        <p style="margin: 0; color: #166534; line-height: 1.6;">
                            📂 <strong>Phòng:</strong> %s<br/>
                            👤 <strong>Chủ sở hữu cũ:</strong> %s (%s)<br/>
                            🚀 <strong>Quyền hạn mới:</strong> Bạn hiện có toàn quyền quản trị và quản lý phòng này.
                        </p>
                    </div>
                    <p style="color: #374151;">
                        Vui lòng truy cập hệ thống để kiểm tra và tiếp tục quản lý phòng.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(newOwner.getUsername(), roomName, roomName, oldOwnerName, oldOwnerEmail);

        emailService.send(newOwner.getEmail(), subject, body);
    }

    @Async
    public void sendRoomUpdatedEmail(RoomEntity room, UserEntity owner) {
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        String roomName = room.getName() != null ? room.getName() : "Direct Message";
        String roomDesc = room.getDescription() != null ? room.getDescription() : "Không có mô tả";
        String subject = "[Synkork] Thông tin phòng " + roomName + " đã được cập nhật";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">ℹ️ Thông tin phòng đã được cập nhật</h2>
                    <p style="color: #374151;">
                        Xin chào <strong>%s</strong>,<br/>
                        Thông tin phòng <strong>%s</strong> của bạn đã được quản trị viên cập nhật.
                    </p>
                    <div style="margin: 24px 0; padding: 16px; background: #f9fafb;
                                border-left: 4px solid #023c3d; border-radius: 8px;">
                        <p style="margin: 0; color: #374151; line-height: 1.6;">
                            📂 <strong>Tên phòng:</strong> %s<br/>
                            📝 <strong>Mô tả mới:</strong> %s<br/>
                            📌 <strong>Trạng thái:</strong> %s
                        </p>
                    </div>
                    <p style="color: #374151;">
                        Nếu bạn không thực hiện yêu cầu này hoặc có thắc mắc, vui lòng liên hệ đội ngũ hỗ trợ Synkork.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(owner.getUsername(), roomName, roomName, roomDesc, room.getStatus());

        emailService.send(owner.getEmail(), subject, body);
    }

    @Async
    public void sendRoomLockedEmail(RoomEntity room, UserEntity owner) {
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        String roomName = room.getName() != null ? room.getName() : "Direct Message";
        String subject = "[Synkork] Thông báo khóa phòng " + roomName;

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #dc2626;">🔒 Thông báo khóa phòng</h2>
                    <p style="color: #374151;">
                        Xin chào <strong>%s</strong>,<br/>
                        Quản trị viên đã khóa phòng <strong>%s</strong> của bạn do phát hiện vi phạm quy định sử dụng của Synkork.
                    </p>
                    <div style="margin: 24px 0; padding: 16px; background: #fef2f2;
                                border-left: 4px solid #ef4444; border-radius: 8px;">
                        <p style="margin: 0; color: #991b1b; line-height: 1.6;">
                            📂 <strong>Phòng bị khóa:</strong> %s<br/>
                            ⚠️ <strong>Trạng thái:</strong> Tạm thời bị khóa (LOCKED)
                        </p>
                    </div>
                    <p style="color: #374151;">
                        Mọi hoạt động truy cập và tương tác trong phòng sẽ bị tạm dừng. Nếu bạn cho rằng đây là một sự nhầm lẫn, vui lòng phản hồi hoặc liên hệ hỗ trợ.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(owner.getUsername(), roomName, roomName);

        emailService.send(owner.getEmail(), subject, body);
    }

    @Async
    public void sendRoomUnlockedEmail(RoomEntity room, UserEntity owner) {
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        String roomName = room.getName() != null ? room.getName() : "Direct Message";
        String subject = "[Synkork] Phòng " + roomName + " của bạn đã được mở khóa";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #023c3d;">🔓 Thông báo mở khóa phòng</h2>
                    <p style="color: #374151;">
                        Xin chào <strong>%s</strong>,<br/>
                        Phòng <strong>%s</strong> của bạn đã được quản trị viên mở khóa thành công.
                    </p>
                    <div style="margin: 24px 0; padding: 16px; background: #f0fdf4;
                                border-left: 4px solid #22c55e; border-radius: 8px;">
                        <p style="margin: 0; color: #166534; line-height: 1.6;">
                            📂 <strong>Phòng mở khóa:</strong> %s<br/>
                            ✅ <strong>Trạng thái:</strong> Đang hoạt động (OPEN)
                        </p>
                    </div>
                    <p style="color: #374151;">
                        Bạn và các thành viên khác hiện đã có thể truy cập và sử dụng phòng bình thường.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(owner.getUsername(), roomName, roomName);

        emailService.send(owner.getEmail(), subject, body);
    }

    @Async
    public void sendRoomWarningEmail(RoomEntity room, UserEntity owner, int warningCount) {
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        String roomName = room.getName() != null ? room.getName() : "Direct Message";
        String subject = "[Synkork] Cảnh báo vi phạm phòng " + roomName;

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;
                            padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                    <h2 style="color: #b45309;">⚠️ Cảnh báo vi phạm phòng</h2>
                    <p style="color: #374151;">
                        Xin chào <strong>%s</strong>,<br/>
                        Quản trị viên đã gửi cảnh báo đến phòng <strong>%s</strong> của bạn.
                    </p>
                    <div style="margin: 24px 0; padding: 16px; background: #fffbeb;
                                border-left: 4px solid #f59e0b; border-radius: 8px;">
                        <p style="margin: 0; color: #92400e; line-height: 1.6;">
                            📂 <strong>Phòng:</strong> %s<br/>
                            ⚠️ <strong>Số lần cảnh báo hiện tại:</strong> %d
                        </p>
                    </div>
                    <p style="color: #374151;">
                        Vui lòng rà soát lại nội dung hoặc hoạt động trong phòng để tuân thủ điều khoản dịch vụ của chúng tôi. Nếu tiếp tục vi phạm, phòng có thể bị khóa vĩnh viễn.
                    </p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0 16px;"/>
                    <p style="margin: 0; font-size: 12px; color: #9ca3af; text-align: center;">
                        Đây là email tự động từ Synkork — vui lòng không reply.
                    </p>
                </div>
                """.formatted(owner.getUsername(), roomName, roomName, warningCount);

        emailService.send(owner.getEmail(), subject, body);
    }
}
