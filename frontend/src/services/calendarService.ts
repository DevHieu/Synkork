import axiosClient from "@/lib/axiosClient";

// Lấy tất cả event theo spaceId
export const getEventsBySpaceId = async (spaceId: string) => {
  const res = await axiosClient.get(`/api/calendar-events/${spaceId}`);
  return res;
};

// Lấy event theo khoảng thời gian
export const getEventsByDateRange = async (
  spaceId: string,
  start: string,
  end: string
) => {
  const res = await axiosClient.get(
    `/api/calendar-events/${spaceId}/range?start=${start}&end=${end}`
  );
  return res;
};

// Lấy event theo ngày cụ thể
export const getEventsByDate = async (spaceId: string, date: string) => {
  const res = await axiosClient.get(`/api/calendar-events/${spaceId}/date?date=${date}`);
  return res;
};

// Tạo event mới
export const createEvent = async (data: any) => {
  const res = await axiosClient.post(`/api/calendar-events`, data);
  return res;
};

// Cập nhật event
export const updateEvent = async (eventId: string, data: any) => {
  const res = await axiosClient.put(`/api/calendar-events/${eventId}`, data);
  return res;
};

export const uploadEventAttachments = async (eventId: string, files: File[]) => {
  const formData = new FormData();
  files.forEach((file) => formData.append("files", file));
  const res = await axiosClient.post(`/api/calendar-events/${eventId}/attachments`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res;
};

export const deleteEvent = async (eventId: string) => {
  const res = await axiosClient.delete(`/api/calendar-events/${eventId}`);
  return res;
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
  const res = await axiosClient.get(url);
  return res;
};
