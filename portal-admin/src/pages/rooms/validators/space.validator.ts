import { z } from 'zod'

import { spaceStatusSchema, spaceTypeSchema } from '../data/schema'

export const spaceValidator = z.object({
  id: z.string().optional(),
  name: z.string().min(1),
  code: z.string().min(1),
  status: spaceStatusSchema,
  type: spaceTypeSchema,
})

export type SpaceValidator = z.infer<typeof spaceValidator>