import dayjs from "dayjs";
import type {
  MessageEventSuggestion,
  SuggestedEventDraft,
} from "@/types/CalendarSuggestion";

const DEFAULT_EVENT_TITLE = "Sự kiện từ tin nhắn";
const DEFAULT_MORNING_START = "07:00";
const DEFAULT_MORNING_END = "12:00";

function normalizeTimeString(time: string | null | undefined): string | null {
  if (!time) return null;

  const matched = time.match(/^(\d{2}):(\d{2})/);
  if (!matched) return null;

  return `${matched[1]}:${matched[2]}`;
}

function addHoursAndClamp(time: string, hours: number): string {
  const [hourText, minuteText] = time.split(":");
  const hour = Number(hourText);
  const minute = Number(minuteText);

  if (Number.isNaN(hour) || Number.isNaN(minute)) {
    return DEFAULT_MORNING_END;
  }

  const totalMinutes = hour * 60 + minute + hours * 60;
  const clampedMinutes = Math.min(totalMinutes, 23 * 60 + 59);
  const nextHour = Math.floor(clampedMinutes / 60)
    .toString()
    .padStart(2, "0");
  const nextMinute = (clampedMinutes % 60).toString().padStart(2, "0");

  return `${nextHour}:${nextMinute}`;
}

function getCurrentTimePlusOneHour(now: dayjs.Dayjs): string {
  return now.add(1, "hour").format("HH:mm");
}

export function buildSuggestedEventDraft(
  suggestion: MessageEventSuggestion,
): SuggestedEventDraft {
  const now = dayjs();
  const today = now.format("YYYY-MM-DD");
  const normalizedEventDate = suggestion.eventDate || today;
  const normalizedStartTime = normalizeTimeString(suggestion.startTime);
  const normalizedEndTime = normalizeTimeString(suggestion.endTime);
  const isToday = normalizedEventDate === today;

  let startTime = normalizedStartTime;
  let endTime = normalizedEndTime;

  if (!startTime) {
    if (isToday) {
      startTime = getCurrentTimePlusOneHour(now);
      endTime = addHoursAndClamp(startTime, 4);
    } else {
      startTime = DEFAULT_MORNING_START;
      endTime = DEFAULT_MORNING_END;
    }
  } else if (!endTime) {
    endTime = addHoursAndClamp(startTime, 4);
  }

  return {
    title: suggestion.title?.trim() || DEFAULT_EVENT_TITLE,
    description: suggestion.description?.trim() || "",
    eventDate: normalizedEventDate,
    startTime,
    endTime: endTime || DEFAULT_MORNING_END,
    allowEditAll: false,
  };
}
