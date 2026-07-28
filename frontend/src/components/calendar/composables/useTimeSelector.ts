import { ref } from "vue";

export type TimeFormat = "24h" | "12h";

/** Parse (HH:mm) -> số phút trong ngày */
export const toMinutes = (time: string): number => {
  const [hStr = "0", mStr = "0"] = (time || "").split(":");
  return (parseInt(hStr, 10) || 0) * 60 + (parseInt(mStr, 10) || 0);
};

/** Format số phút -> (HH:mm) */
export const fromMinutes = (totalMinutes: number): string => {
  const clamped = Math.min(Math.max(0, totalMinutes), 23 * 60 + 59);
  const hh = Math.floor(clamped / 60).toString().padStart(2, "0");
  const mm = (clamped % 60).toString().padStart(2, "0");
  return `${hh}:${mm}`;
};

// Options cố định cho UI Select (module scope)
export const hours24 = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, "0"));
export const hours12 = Array.from({ length: 12 }, (_, i) => (i + 1).toString().padStart(2, "0"));
export const minutes = Array.from({ length: 60 }, (_, i) => i.toString().padStart(2, "0"));

export function useTimeSelector() {
  const timeFormat = ref<TimeFormat>("24h");

  // Tách chuỗi HH:mm thành { hour, minute, ampm }
  const parseTime = (timeStr: string = "09:00", format: TimeFormat = "24h") => {
    const [hStr = "00", mStr = "00"] = timeStr.split(":");
    let h = parseInt(hStr, 10) || 0;
    const ampm = h >= 12 ? "PM" : "AM";
    
    if (format === "12h") {
      if (h > 12) h -= 12;
      if (h === 0) h = 12;
    }
    
    return {
      hour: h.toString().padStart(2, "0"),
      minute: mStr,
      ampm,
    };
  };

  // Gộp { hour, minute, ampm } thành chuỗi 24h HH:mm
  const formatTime = (hour: string, minute: string, ampm: string = "AM", format: TimeFormat = "24h"): string => {
    const m = (minute || "00").padStart(2, "0");
    let h = parseInt(hour, 10) || 0;
    if (format === "12h") {
      if (ampm === "PM" && h < 12) h += 12;
      if (ampm === "AM" && h === 12) h = 0;
    }
    return `${h.toString().padStart(2, "0")}:${m}`;
  };

  // Tự động đẩy endTime lên +1h nếu endTime <= startTime
  const adjustEndTimeIfNeeded = (startTime: string, endTime: string): string => {
    return toMinutes(endTime) <= toMinutes(startTime)
      ? fromMinutes(toMinutes(startTime) + 60)
      : endTime;
  };

  return {
    timeFormat,
    hours24,
    hours12,
    minutes,
    parseTime,
    formatTime,
    adjustEndTimeIfNeeded,
  };
}
