import axiosClient from "@/lib/axiosClient";
import axios from "axios";

// Lấy tất cả event theo spaceId
export const getEventsBySpaceId = async (spaceId: string) => {
  return await axiosClient.get(`/api/calendar-events/${spaceId}`);
};

// Lấy event theo khoảng thời gian
export const getEventsByDateRange = async (
  spaceId: string,
  start: string,
  end: string
) => {
  return await axiosClient.get(
    `/api/calendar-events/${spaceId}/range?start=${start}&end=${end}`
  );
};

// Lấy event theo ngày cụ thể
export const getEventsByDate = async (spaceId: string, date: string) => {
  return await axiosClient.get(`/api/calendar-events/${spaceId}/date?date=${date}`);
};

// Tạo event mới
export const createEvent = async (data: any) => {
  return await axiosClient.post(`/api/calendar-events`, data);
};

// Cập nhật event
export const updateEvent = async (eventId: string, data: any) => {
  try {
    return await axiosClient.put(`/api/calendar-events/${eventId}`, data);
  } catch (e: any) {
    if (axios.isAxiosError(e) && e.response?.status === 409) {
      throw new CalendarVersionConflictError();
    }
    throw e;
  }
};

export const uploadEventAttachments = async (eventId: string, files: File[]) => {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));
  return await axiosClient.post(`/api/calendar-events/${eventId}/attachments`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const deleteEvent = async (eventId: string) => {
  return await axiosClient.delete(`/api/calendar-events/${eventId}`);
};

// Kiểm tra sự kiện trùng giờ
export const checkConflicts = async (
  spaceId: string,
  date: string,
  endDate: string,
  startTime: string,
  endTime: string,
  excludeId?: string
) => {
  let url = `/api/calendar-events/${spaceId}/conflicts?date=${date}&endDate=${endDate}&startTime=${startTime}&endTime=${endTime}`;
  if (excludeId) url += `&excludeId=${excludeId}`;
  return await axiosClient.get(url);
};

// Tóm tắt nội dung file đính kèm bằng AI
export const summarizeAttachment = async (eventId: string, attachmentId: string) => {
  return await axiosClient.post(
    `/api/calendar-events/${eventId}/attachments/${attachmentId}/summarize`,
    {}, // Không có body
    { timeout: 60000 } // Tăng timeout lên 60s cho xử lý AI
  );
};

export class CalendarVersionConflictError extends Error {
  constructor() {
    super("CALENDAR_VERSION_CONFLICT");
    this.name = "CalendarVersionConflictError";
    Object.setPrototypeOf(this, CalendarVersionConflictError.prototype);
  }
}
