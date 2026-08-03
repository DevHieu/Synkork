export const avatarColors = [
    'bg-rose-100 text-rose-600',
    'bg-sky-100 text-sky-600',
    'bg-violet-100 text-violet-600',
    'bg-amber-100 text-amber-600',
    'bg-emerald-100 text-emerald-600',
    'bg-pink-100 text-pink-600',
] as const

export function getAvatarColor(name?: string) {
    if (!name) return avatarColors[0]
    const idx = name.charCodeAt(0) % avatarColors.length
    return avatarColors[idx]
}

export function getInitials(name?: string) {
    if (!name?.trim()) return '?'
    const parts = name.trim().split(' ')
    if (parts.length === 1) return parts[0]?.substring(0, 2).toUpperCase() ?? '?'
    return ((parts[0]?.[0] ?? '') + (parts[parts.length - 1]?.[0] ?? '')).toUpperCase()
}
