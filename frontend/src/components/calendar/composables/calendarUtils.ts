import { unref } from "vue";
import type { Ref } from "vue";
import type dayjs from "dayjs";

/**
 * Tính khoảng ngày cần fetch dữ liệu theo chế độ xem lịch.
 */
export const calculateDateRange = (date: dayjs.Dayjs, mode: string) => {
  if (mode === "week") {
    return {
      start: date.startOf("week").format("YYYY-MM-DD"),
      end: date.endOf("week").format("YYYY-MM-DD")
    };
  }

  if (mode === "year") {
    return {
      start: date.startOf("year").format("YYYY-MM-DD"),
      end: date.endOf("year").format("YYYY-MM-DD")
    };
  }

  // View month default (đệm 7 ngày)
  return {
    start: date.startOf("month").subtract(7, "day").format("YYYY-MM-DD"),
    end: date.endOf("month").add(7, "day").format("YYYY-MM-DD")
  };
};

/**
 * Chuẩn hóa link sự kiện: trim rồi trả về null nếu rỗng.
 */
export const normalizeEventLink = (eventLink?: string) => {
  const trimmedLink = eventLink?.trim();
  return trimmedLink || null;
};

/**
 * Trích xuất các file mới (chưa upload) từ danh sách attachments.
 */
export const extractNewFiles = (data: any): File[] => {
  if (!Array.isArray(data.attachments)) return [];
  return data.attachments
    .map((attachment: any) => attachment?.file)
    .filter((file: File | undefined): file is File => file instanceof File);
};

/**
 * Chuẩn hóa payload trước khi gửi API tạo/sửa event.
 */
export const formatPayload = (
  data: any,
  spaceIdRef: Ref<string | undefined>,
  currentUserId: any,
  id?: string,
) => {
  const normalizedAttendeeIds = data.attendeeIds?.filter((id: string) => Boolean(id)) ?? [];

  const normalizedAttachments = Array.isArray(data.attachments)
    ? data.attachments
        .filter((attachment: any) => attachment?.name && !attachment?.file)
        .map((attachment: any) => ({
          name: attachment.name,
          size: attachment.size || 0,
          fileUrl: attachment.fileUrl ?? "",
          publicId: attachment.publicId ?? "",
          resourceType: attachment.resourceType ?? "",
          type: attachment.type,
        }))
    : [];

  const payload = {
    ...data,
    eventLink: normalizeEventLink(data.eventLink),
    endDate: data.endDate || data.eventDate,
    startTime: data.startTime.length === 5 ? `${data.startTime}:00` : data.startTime,
    endTime: data.endTime.length === 5 ? `${data.endTime}:00` : data.endTime,
    spaceId: spaceIdRef.value,
    createdById: unref(currentUserId),
    attendeeIds: normalizedAttendeeIds,
    attachments: normalizedAttachments,
  };
  delete payload.attendees;
  if (id) payload.id = id;
  // Xóa recurrenceEndDate khi không áp dụng
  if (payload.recurrenceType === 'NONE' || !payload.recurrenceEndDate) {
    delete payload.recurrenceEndDate;
  }
  return payload;
};
