import { ref } from "vue";

// State & Utils thời gian
export type TimeFormat = "24h" | "12h";

// Mock data dropdown
const buildHours24 = (): string[] =>
  Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, "0"));

const buildHours12 = (): string[] =>
  Array.from({ length: 12 }, (_, i) => (i + 1).toString().padStart(2, "0"));

const buildMinutes = (): string[] =>
  Array.from({ length: 60 }, (_, i) => i.toString().padStart(2, "0"));

/** Parse (HH:mm) -> số phút */
export const toMinutes = (time: string): number => {
  const [hStr, mStr] = time.split(":");
  if (!hStr || !mStr) return 0;
  return parseInt(hStr, 10) * 60 + parseInt(mStr, 10);
};

/** Format phút -> (HH:mm) */
const fromMinutes = (totalMinutes: number): string => {
  const clamped = Math.min(totalMinutes, 23 * 60 + 59);
  const hh = Math.floor(clamped / 60).toString().padStart(2, "0");
  const mm = (clamped % 60).toString().padStart(2, "0");
  return `${hh}:${mm}`;
};

export function useTimeSelector() {
  const timeFormat = ref<TimeFormat>("24h");

  const hours24 = buildHours24();
  const hours12 = buildHours12();
  const minutes = buildMinutes();

  // State UI
  const startHour = ref("09");
  const startMinute = ref("00");
  const startAmPm = ref("AM");

  const endHour = ref("10");
  const endMinute = ref("00");
  const endAmPm = ref("AM");

  // Parse (24h)
  const parseInto24h = (timeStr: string): { h24: string; m: string; ampm: string } => {
    const [hStr = "00", mStr = "00"] = timeStr.split(":");
    const h = parseInt(hStr, 10);
    const ampm = h >= 12 ? "PM" : "AM";
    const h24 = h.toString().padStart(2, "0");
    return { h24, m: mStr, ampm };
  };

  // Parse (12h)
  const parseInto12h = (timeStr: string): { h12: string; m: string; ampm: string } => {
    const [hStr = "00", mStr = "00"] = timeStr.split(":");
    let h = parseInt(hStr, 10);
    const ampm = h >= 12 ? "PM" : "AM";
    if (h > 12) h -= 12;
    if (h === 0) h = 12;
    return { h12: h.toString().padStart(2, "0"), m: mStr, ampm };
  };

  // Update refs từ chuỗi đầu vào
  const parseTimeString = (timeStr: string | undefined, isStart: boolean): void => {
    if (!timeStr) return;

    if (timeFormat.value === "24h") {
      const { h24, m, ampm } = parseInto24h(timeStr);
      if (isStart) { startHour.value = h24; startMinute.value = m; startAmPm.value = ampm; }
      else          { endHour.value   = h24; endMinute.value   = m; endAmPm.value   = ampm; }
    } else {
      const { h12, m, ampm } = parseInto12h(timeStr);
      if (isStart) { startHour.value = h12; startMinute.value = m; startAmPm.value = ampm; }
      else          { endHour.value   = h12; endMinute.value   = m; endAmPm.value   = ampm; }
    }
  };

  // Format HH:mm string
  const buildTimeString = (hour: string, minute: string, ampm: string): string => {
    if (timeFormat.value === "24h") return `${hour}:${minute}`;
    let h = parseInt(hour, 10);
    if (ampm === "PM" && h < 12) h += 12;
    if (ampm === "AM" && h === 12) h = 0;
    return `${h.toString().padStart(2, "0")}:${minute}`;
  };

  // Validate End sau Start
  const adjustEndTimeIfNeeded = (startTime: string, endTime: string): string => {
    if (toMinutes(endTime) <= toMinutes(startTime)) {
      return fromMinutes(toMinutes(startTime) + 60);
    }
    return endTime;
  };

  // Sync format chéo 12h/24h
  const syncDropdownsOnFormatChange = (startTime: string, endTime: string) => {
    parseTimeString(startTime, true);
    parseTimeString(endTime, false);
  };

  return {
    timeFormat,
    hours24,
    hours12,
    minutes,
    startHour, startMinute, startAmPm,
    endHour,   endMinute,   endAmPm,
    parseTimeString,
    buildTimeString,
    adjustEndTimeIfNeeded,
    syncDropdownsOnFormatChange,
  };
}
