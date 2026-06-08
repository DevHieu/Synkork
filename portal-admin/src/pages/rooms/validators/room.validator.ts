import { z } from 'zod'

export const roomValidator = z.object({
  id: z.string().optional(),
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  status: z.enum(['OPEN', 'CLOSED']),
  ownerId: z.string().min(1, 'Owner is required'),
})

export type RoomValidator = z.infer<typeof roomValidator>