<script lang="ts" setup>
import { reactive } from 'vue'
import axiosClient from '@/lib/axiosClient'
import { toTypedSchema } from '@vee-validate/zod'
import { useForm } from 'vee-validate'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

import type { Room } from '../data/schema'
import { roomValidator } from '../validators/room.validator'

const { room } = defineProps<{ room?: Room }>()

const emits = defineEmits<{
  (e: 'close'): void
  (e: 'refresh'): void
}>()

const statuses = [
  { value: 'OPEN', label: 'Open' },
  { value: 'CLOSED', label: 'Closed' },
]

const initialValues = reactive({
  name: room?.name || '',
  description: room?.description || '',
  status: room?.status || 'OPEN',
  ownerId: '',
})

const { handleSubmit, resetForm } = useForm({
  validationSchema: toTypedSchema(roomValidator),
  initialValues,
})

const onSubmit = handleSubmit(async (values) => {
  try {
    if (room?.id) {
      await axiosClient.put(`/manage/rooms/${room.id}`, values)
      toast.success('Room đã được cập nhật')
    } else {
      await axiosClient.post('/manage/rooms', values)
      toast.success('Room đã được tạo thành công')
    }
    emits('refresh')
    emits('close')
    resetForm()
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi tạo/cập nhật room')
  }
})
</script>

<template>
  <div class="max-h-[500px] overflow-y-auto">
    <form class="space-y-4" @submit="onSubmit">
      <FormField v-slot="{ componentField }" name="name">
        <FormItem>
          <FormLabel>Name</FormLabel>
          <FormControl><Input v-bind="componentField" placeholder="Room name" /></FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <FormField v-slot="{ componentField }" name="description">
        <FormItem>
          <FormLabel>Description</FormLabel>
          <FormControl><Textarea v-bind="componentField" placeholder="Optional description" /></FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <FormField v-slot="{ componentField }" name="status">
        <FormItem>
          <FormLabel>Status</FormLabel>
          <Select v-bind="componentField">
            <FormControl>
              <SelectTrigger class="w-full">
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              <SelectGroup>
                <SelectItem v-for="s in statuses" :key="s.value" :value="s.value">
                  {{ s.label }}
                </SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      </FormField>

      <!-- Chỉ hiện khi tạo mới -->
      <FormField v-if="!room?.id" v-slot="{ componentField }" name="ownerId">
        <FormItem>
          <FormLabel>Owner ID</FormLabel>
          <FormControl><Input v-bind="componentField" placeholder="UUID của owner" /></FormControl>
          <FormMessage />
        </FormItem>
      </FormField>

      <Button type="submit" class="w-full">
        {{ room?.id ? 'Update Room' : 'Create Room' }}
      </Button>
    </form>
  </div>
</template>