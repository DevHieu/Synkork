export function formattedDate(date?: string | undefined) {
    if (!date) return null
    return new Date(date).toLocaleDateString('vi-VN', {
        day: '2-digit', month: '2-digit', year: 'numeric'
    })
}

export const checkDueSoon = ((dueDate?: string | undefined) => {
    if (!dueDate) return false
    const due = new Date(dueDate)
    const now = new Date()
    const diff = (due.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
    return diff <= 2 && diff >= 0
})

export const checkOverdue = ((dueDate?: string) => {
    if (!dueDate) return false
    return new Date(dueDate) < new Date()
})