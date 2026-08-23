import { useMutation, useQueryClient } from '@tanstack/vue-query'

import { authService } from '@/pages/auth/services/authService'
import type { ChangePasswordData } from '@/pages/auth/types/ChangePasswordData'
import type { UpdateProfileData } from '@/pages/auth/types/UpdateProfileData'
import type { Account } from '@/types/Account'

import type { IResponse } from '../types/response.type'

export function useUpdateProfileMutation() {
  const queryClient = useQueryClient()

  return useMutation<IResponse<Account>, Error, UpdateProfileData>({
    mutationKey: ['useUpdateProfileMutation'],
    mutationFn: async (data: UpdateProfileData) => {
      return await authService.updateProfile(data)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['useGetUserInfoQuery'] })
    },
  })
}

export function useChangePasswordMutation() {
  return useMutation<IResponse<{ message: string }>, Error, ChangePasswordData>({
    mutationKey: ['useChangePasswordMutation'],
    mutationFn: async (data: ChangePasswordData) => {
      return await authService.changePassword(data)
    },
  })
}

