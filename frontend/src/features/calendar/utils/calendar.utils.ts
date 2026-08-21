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

  // View month default: lấy trọn vẹn từ đầu tuần của ngày 1 đến hết tuần của ngày cuối tháng (bao gồm ngày đệm)
  return {
    start: date.startOf("month").startOf("week").subtract(7, "day").format("YYYY-MM-DD"),
    end: date.endOf("month").endOf("week").add(7, "day").format("YYYY-MM-DD")
  };
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
    eventLink: data.eventLink?.trim() || null,
    endDate: data.endDate || data.eventDate,
    startTime: data.startTime && data.startTime.length === 5 ? `${data.startTime}:00` : (data.startTime || "09:00:00"),
    endTime: data.endTime && data.endTime.length === 5 ? `${data.endTime}:00` : (data.endTime || "10:00:00"),
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
