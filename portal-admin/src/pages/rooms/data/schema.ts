import { z } from 'zod'

// ─── Room Status & Type khớp với BE ───────────────────────────────────────────
export const roomStatusSchema = z.enum(['OPEN', 'CLOSED']).or(z.null()).transform(v => v ?? 'OPEN')
export type RoomStatus = 'OPEN' | 'CLOSED'

export const roomTypeSchema = z.enum(['GROUP', 'DM'])
export type RoomType = z.infer<typeof roomTypeSchema>

// ─── Space bên trong Room (từ AdminRoomDetailResponse) ────────────────────────
export const spaceSchema = z.object({
  id: z.string(),
  name: z.string(),
  type: z.string(),
})
export type Space = z.infer<typeof spaceSchema>

// ─── Member bên trong Room (từ AdminRoomDetailResponse) ───────────────────────
export const memberSchema = z.object({
  id: z.string(),
  username: z.string(),
  email: z.string(),
  avatarUrl: z.string().nullable().optional(),
  role: z.string(),
  joinedAt: z.coerce.date(),
})
export type Member = z.infer<typeof memberSchema>

// ─── Room list item (từ AdminRoomResponse) ────────────────────────────────────
export const roomSchema = z.object({
  id: z.string(),
  name: z.string(),
  avatarUrl: z.string().nullable().optional(),
  description: z.string().nullable().optional(),
  type: roomTypeSchema,
  status: roomStatusSchema,
  inviteCode: z.string().nullable().optional(),
  memberCount: z.number().default(0),
})
export type Room = z.infer<typeof roomSchema>
export const roomListSchema = z.array(roomSchema)

// ─── Room detail (từ AdminRoomDetailResponse) ─────────────────────────────────
export const roomDetailSchema = roomSchema.extend({
  createdAt: z.coerce.date(),
  updatedAt: z.coerce.date(),
  owner: z.object({
    id: z.string(),
    username: z.string(),
    email: z.string(),
    avatarUrl: z.string().nullable().optional(),
  }).nullable().optional(),
  members: z.array(memberSchema).default([]),
  spaces: z.array(spaceSchema).default([]),
})
export type RoomDetail = z.infer<typeof roomDetailSchema>