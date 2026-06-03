import dayjs from "dayjs";

export const formatPickerToDate = (date: Date) =>
  dayjs(date).format("YYYYMMDD");

export const formatTimestamp = (
  value: number | string | null | undefined,
) => {
  if (!value) return "-";
  const date = typeof value === "number" ? new Date(value) : new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : dayjs(date).format("HH:mm dd/MM/yyyy");
};
