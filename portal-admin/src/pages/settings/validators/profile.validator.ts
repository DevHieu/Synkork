import { z } from 'zod'

export const profileValidator = z.object({
  username: z
    .string()
    .min(2, {
      message: 'Username phải có ít nhất 2 ký tự.',
    })
    .max(30, {
      message: 'Username không được dài hơn 30 ký tự.',
    }),
  displayName: z
    .string()
    .min(2, {
      message: 'Tên hiển thị phải có ít nhất 2 ký tự.',
    })
    .max(60, {
      message: 'Tên hiển thị không được dài hơn 60 ký tự.',
    }),
})

export const changePasswordValidator = z.object({
  currentPassword: z
    .string()
    .min(1, {
      message: 'Vui lòng nhập mật khẩu hiện tại.',
    }),
  newPassword: z
    .string()
    .min(6, {
      message: 'Mật khẩu mới phải có ít nhất 6 ký tự.',
    }),
  confirmPassword: z
    .string()
    .min(1, {
      message: 'Vui lòng xác nhận mật khẩu mới.',
    }),
}).refine(data => data.newPassword === data.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp.',
  path: ['confirmPassword'],
})

export type ProfileValidator = z.infer<typeof profileValidator>
export type ChangePasswordValidator = z.infer<typeof changePasswordValidator>

