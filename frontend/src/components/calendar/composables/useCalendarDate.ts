import { ref, computed } from "vue";
import dayjs from "dayjs";

// Quản lý ngày giờ, UI view
export function useCalendarDate() {
  const viewMode = ref<"week" | "month" | "year">("month");
  const currentDate = ref(dayjs());
  const selectedDate = ref(dayjs());

  // Điều chỉnh ngày (nhảy tới/lui)
  const jumpDate = (amount: number, unit: "week" | "month" | "year") => {
    currentDate.value = currentDate.value.add(amount, unit);
  };

  const goNext = () => {
    jumpDate(1, viewMode.value);
  };

  const goPrev = () => {
    jumpDate(-1, viewMode.value);
  };

  const goToday = () => {
    currentDate.value = dayjs();
    selectedDate.value = dayjs();
  };

  const selectDate = (date: dayjs.Dayjs) => {
    selectedDate.value = date;
  };

  // Chọn tháng, chuyển view month
  const setYearMonth = (monthIndex: number) => {
    currentDate.value = currentDate.value.month(monthIndex);
    viewMode.value = "month";
  };

  // Helpers dùng chung cho các view
  const dayNames = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];
  const dayNamesLong = ["CN", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
  const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");
  const isSelected = (date: dayjs.Dayjs) => date.isSame(selectedDate.value, "day");

  // Tiêu đề header
  const headerTitle = computed(() => {
    if (viewMode.value === "week") {
      const start = currentDate.value.startOf("week");
      const end = currentDate.value.endOf("week");
      return `${start.format("DD/MM")} - ${end.format("DD/MM/YYYY")}`;
    }
    if (viewMode.value === "year") return currentDate.value.format("YYYY");
    const formatted = currentDate.value.format("MMMM YYYY");
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  });

  // Lookup table: [zero, prev, next] — dùng chung cho week/month
  const RELATIVE_LABELS: Record<"week" | "month", [string, string, string]> = {
    week:  ["Tuần này",  "Tuần trước",  "Tuần sau"],
    month: ["Tháng này", "Tháng trước", "Tháng sau"],
  };

  // Factory: chuyển diff số -> chuỗi tương đối
  const relativeLabel = (diff: number, [zero, prev, next]: [string, string, string]): string => {
    if (diff === 0)  return zero;
    if (diff === -1) return prev;
    if (diff === 1)  return next;
    return diff < 0 ? `${-diff} ${prev}` : `${diff} ${next}`;
  };

  // Label thời gian tương đối
  const relativeTimeText = computed(() => {
    const now = dayjs();
    if (viewMode.value === "year") {
      const diff = currentDate.value.year() - now.year();
      if (diff === 0)  return "Năm nay";
      if (diff === -1) return "Năm ngoái";
      if (diff === 1)  return "Năm sau";
      return `Năm ${currentDate.value.year()}`;
    }
    const unit = viewMode.value; // "week" | "month"
    const diff = currentDate.value.startOf(unit).diff(now.startOf(unit), unit);
    return relativeLabel(diff, RELATIVE_LABELS[unit]);
  });

  return {
    viewMode,
    currentDate,
    selectedDate,
    headerTitle,
    relativeTimeText,
    goNext,
    goPrev,
    goToday,
    selectDate,
    setYearMonth,
    dayNames,
    dayNamesLong,
    isToday,
    isSelected,
  };
}
