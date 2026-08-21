const icon = {
  calendar: `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/></svg>`,
  clock: `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
  document: `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M10 9H8"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>`,
  check: `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m9 12 2 2 4-4"/></svg>`,
};

const warning = `<div class="mt-4 pt-3 border-t border-border/60 text-[18px] font-semibold text-destructive flex items-start italic leading-tight"><svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg><span>Nội dung được tóm tắt bởi AI có thể không chính xác 100%, bạn nên kiểm tra lại tài liệu gốc nếu có thể.</span></div>`;

export const buildAttachmentSummaryHtml = (value: unknown): string => {
  let data = value;
  if (typeof data === "string") {
    try { data = JSON.parse(data); } catch { return `${data}${warning}`; }
  }

  let html = "";
  if (data && typeof data === "object") {
    const summary = data as Record<string, unknown>;
    const row = (iconHtml: string, label: string, content: unknown, className: string) =>
      content ? `<div class="${className} flex items-start"><span class="shrink-0 text-primary mt-0.5 mr-1">${iconHtml}</span><div><strong>${label}:</strong> ${content}</div></div>` : "";
    html += row(icon.calendar, "Tên sự kiện", summary.event_name, "mb-2");
    html += row(icon.clock, "Thời gian & Địa điểm", summary.time_location, "mb-2");
    html += row(icon.document, "Tóm tắt", summary.summary, "mb-3");

    if (Array.isArray(summary.action_items) && summary.action_items.length) {
      const items = summary.action_items.map((item) => {
        if (typeof item === "string") return item;
        if (item && typeof item === "object") {
          const text = Object.values(item).filter((part): part is string => typeof part === "string").join(" - ");
          return text || JSON.stringify(item);
        }
        return "";
      }).filter(Boolean).map((item) => `<li>${item}</li>`).join("");
      html += `<div class="mb-1 flex items-center"><span class="shrink-0 text-primary mr-1">${icon.check}</span><strong>Công việc cần làm:</strong></div><ul class="list-disc pl-5 mb-2 ml-5">${items}</ul>`;
    }
    if (!html) html = `<pre class="text-xs whitespace-pre-wrap">${JSON.stringify(summary, null, 2)}</pre>`;
  } else {
    html = String(data ?? "");
  }
  return `<div class="font-sans text-sm text-foreground max-h-[60vh] overflow-y-auto calendar-scrollbar pr-2">${html}${warning}</div>`;
};
