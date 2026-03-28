import { ref, computed } from "vue";
import dayjs from "dayjs";

export function useCalendarDate() {
  const viewMode = ref<"week" | "month" | "year">("month");
  const currentDate = ref(dayjs());
  const selectedDate = ref(dayjs());

  // Chuyển sang trang tiếp theo (Tuần/Tháng/Năm)
  const goNext = () => {
    if (viewMode.value === "week") currentDate.value = currentDate.value.add(1, "week");
    else if (viewMode.value === "month") currentDate.value = currentDate.value.add(1, "month");
    else currentDate.value = currentDate.value.add(1, "year");
  };

  // Quay lại trang trước (Tuần/Tháng/Năm)
  const goPrev = () => {
    if (viewMode.value === "week") currentDate.value = currentDate.value.subtract(1, "week");
    else if (viewMode.value === "month") currentDate.value = currentDate.value.subtract(1, "month");
    else currentDate.value = currentDate.value.subtract(1, "year");
  };

  // Về ngày hiện tại
  const goToday = () => {
    currentDate.value = dayjs();
    selectedDate.value = dayjs();
  };

  // Chọn một ngày cụ thể
  const selectDate = (date: dayjs.Dayjs) => {
    selectedDate.value = date;
  };

  // Chuyển sang tháng cụ thể (từ màn hình xem Năm)
  const setYearMonth = (monthIndex: number) => {
    currentDate.value = currentDate.value.month(monthIndex);
    viewMode.value = "month";
  };

  // Tiêu đề hiển thị trên thanh điều hướng
  const headerTitle = computed(() => {
    if (viewMode.value === "week") {
      const start = currentDate.value.startOf("week");
      const end = currentDate.value.endOf("week");
      return `${start.format("DD/MM")} - ${end.format("DD/MM/YYYY")}`;
    } else if (viewMode.value === "year") {
      return currentDate.value.format("YYYY");
    }
    return currentDate.value.format("MMMM YYYY");
  });

  // Văn bản thời gian tương đối (Hôm nay/Tuần này/Tháng này...)
  const relativeTimeText = computed(() => {
    const now = dayjs();
    if (viewMode.value === "week") {
      const diff = currentDate.value.startOf("week").diff(now.startOf("week"), "week");
      if (diff === 0) return "Tuần này";
      if (diff === -1) return "Tuần trước";
      if (diff === 1) return "Tuần sau";
      if (diff < -1) return `${-diff} tuần trước`;
      return `${diff} tuần sau`;
    } else if (viewMode.value === "month") {
      const diff = currentDate.value.startOf("month").diff(now.startOf("month"), "month");
      if (diff === 0) return "Tháng này";
      if (diff === -1) return "Tháng trước";
      if (diff === 1) return "Tháng sau";
      if (diff < -1) return `${-diff} tháng trước`;
      return `${diff} tháng sau`;
    } else {
      const diff = currentDate.value.year() - now.year();
      if (diff === 0) return "Năm nay";
      if (diff === -1) return "Năm ngoái";
      if (diff === 1) return "Năm sau";
      return `Năm ${currentDate.value.year()}`;
    }
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
  };
}
