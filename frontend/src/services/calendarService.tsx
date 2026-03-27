import axios from "axios";

// Lấy tất cả event theo spaceId
export const getEventsBySpaceId = async (spaceId: string) => {
  const res = await axios.get(`/calendar-events/${spaceId}`);
  return res;
};

// Lấy event theo khoảng thời gian
export const getEventsByDateRange = async (
  spaceId: string,
  start: string,
  end: string
) => {
  const res = await axios.get(
    `/calendar-events/${spaceId}/range?start=${start}&end=${end}`
  );
  return res;
};

// Lấy event theo ngày cụ thể
export const getEventsByDate = async (spaceId: string, date: string) => {
  const res = await axios.get(`/calendar-events/${spaceId}/date?date=${date}`);
  return res;
};

// Tạo event mới
export const createEvent = async (data: any) => {
  const res = await axios.post(`/calendar-events`, data);
  return res;
};

// Cập nhật event
export const updateEvent = async (eventId: string, data: any) => {
  const res = await axios.put(`/calendar-events/${eventId}`, data);
  return res;
};

// Xóa event
export const deleteEvent = async (eventId: string, userId: string) => {
  const res = await axios.delete(
    `/calendar-events/${eventId}?userId=${userId}`
  );
  return res;
};

// Kiểm tra sự kiện trùng giờ
export const checkConflicts = async (
  spaceId: string,
  date: string,
  startTime: string,
  endTime: string,
  excludeId?: string
) => {
  let url = `/calendar-events/${spaceId}/conflicts?date=${date}&startTime=${startTime}&endTime=${endTime}`;
  if (excludeId) url += `&excludeId=${excludeId}`;
  const res = await axios.get(url);
  return res;
};

