<script lang="ts" setup>
import { toTypedSchema } from '@vee-validate/zod'
import { useForm } from 'vee-validate'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

import type { User } from '../data/schema'
import type { UserValidator } from '../validators/user.validator'

import { adminUserService } from '../data/userAdminService'
import { userValidator } from '../validators/user.validator'

const { user } = defineProps<{
  user?: User
}>()

const emits = defineEmits<{
  (e: 'close'): void
  (e: 'saved', user: User): void
}>()

const isLoading = ref(false)

const roles = ['admin', 'manager', 'user'] as const
const status = ['active', 'inactive', 'invited', 'suspended'] as const

const initialValues = reactive<UserValidator>({
  firstName: user?.firstName || '',
  lastName: user?.lastName || '',
  username: user?.username || '',
  email: user?.email || '',
  status: user?.status || 'active',
  role: user?.role || 'admin',
})

const userFormSchema = toTypedSchema(userValidator)
const { handleSubmit } = useForm({
  validationSchema: userFormSchema,
  initialValues,
})

const onSubmit = handleSubmit(async (values) => {
  isLoading.value = true
  try {
    let result: User
    if (user?.id) {
      result = await adminUserService.update(user.id, values)
      toast.success('Cập nhật người dùng thành công')
    }
    else {
      result = await adminUserService.create(values)
      toast.success('Tạo người dùng thành công')
    }
    emits('saved', result)
    emits('close')
  }
  catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || 'Có lỗi xảy ra'
    toast.error(msg)
  }
  finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="max-h-[500px] overflow-y-auto">
    <form class="space-y-8" @submit="onSubmit">
      <FormField v-slot="{ componentField }" name="firstName">
        <FormItem>
          <FormLabel>First Name</FormLabel>
          <FormControl>
            <Input type="text" v-bind="componentField" />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>
      <FormField v-slot="{ componentField }" name="lastName">
        <FormItem>
          <FormLabel>Last Name</FormLabel>
          <FormControl>
            <Input type="text" v-bind="componentField" />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>
      <FormField v-slot="{ componentField }" name="username">
        <FormItem>
          <FormLabel>User Name</FormLabel>
          <FormControl>
            <Input type="text" v-bind="componentField" :disabled="!!user?.id" />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <FormField v-slot="{ componentField }" name="email">
        <FormItem>
          <FormLabel>Email address</FormLabel>
          <FormControl>
            <Input type="text" v-bind="componentField" />
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <FormField v-slot="{ componentField }" name="status">
        <FormItem>
          <FormLabel>Status</FormLabel>
          <FormControl>
            <Select v-bind="componentField">
              <FormControl>
                <SelectTrigger class="w-full">
                  <SelectValue placeholder="Select a status" />
                </SelectTrigger>
              </FormControl>
              <SelectContent>
                <SelectGroup>
                  <SelectItem v-for="state in status" :key="state" :value="state">
                    {{ state }}
                  </SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <FormField v-slot="{ componentField }" name="role">
        <FormItem>
          <FormLabel>Role</FormLabel>
          <FormControl>
            <Select v-bind="componentField">
              <FormControl>
                <SelectTrigger class="w-full">
                  <SelectValue placeholder="Select a role" />
                </SelectTrigger>
              </FormControl>
              <SelectContent>
                <SelectGroup>
                  <SelectItem v-for="role in roles" :key="role" :value="role">
                    {{ role }}
                  </SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
          </FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <Button type="submit" class="w-full" :disabled="isLoading">
        {{ isLoading ? 'Đang lưu...' : 'SaveChanges' }}
      </Button>
    </form>
  </div>
</template>
