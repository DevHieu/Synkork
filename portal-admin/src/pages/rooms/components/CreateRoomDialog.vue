<script setup lang="ts">
import { refDebounced } from '@vueuse/core'
import { ref, watch } from 'vue'

import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input as UiInput } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea as UiTextarea } from '@/components/ui/textarea'

import type { RoomFormPayload, UserOption } from '../types/RoomTypes'

import { roomService } from '../service/roomService'

const emit = defineEmits<{
  created: []
}>()

const open = defineModel<boolean>('open', { required: true })

const form = ref<RoomFormPayload>({
  name: '',
  description: '',
  status: 'OPEN',
  ownerId: undefined,
})

const ownerKeyword = ref('')
const ownerOptions = ref<UserOption[]>([])
const selectedOwner = ref<UserOption | null>(null)
const isSearchingOwner = ref(false)
const showOwnerDropdown = ref(false)
const debouncedOwnerKeyword = refDebounced(ownerKeyword, 400)

const isSubmitting = ref(false)
const formError = ref('')

function resetForm() {
  form.value = {
    name: '',
    description: '',
    status: 'OPEN',
    ownerId: undefined,
  }
  selectedOwner.value = null
  ownerKeyword.value = ''
  ownerOptions.value = []
  showOwnerDropdown.value = false
  formError.value = ''
}

// Reset form mỗi khi dialog được mở lại
watch(open, (value) => {
  if (value)
    resetForm()
})

function pickOwner(user: UserOption) {
  selectedOwner.value = user
  form.value.ownerId = user.id
  ownerKeyword.value = ''
  ownerOptions.value = []
  showOwnerDropdown.value = false
}

function clearOwner() {
  selectedOwner.value = null
  form.value.ownerId = undefined
}

watch(debouncedOwnerKeyword, async (keyword) => {
  if (!keyword.trim()) {
    ownerOptions.value = []
    return
  }

  isSearchingOwner.value = true
  try {
    ownerOptions.value = await roomService.searchOwners(keyword.trim())
  }
  finally {
    isSearchingOwner.value = false
  }
})

async function handleSubmitCreate() {
  formError.value = ''

  if (!form.value.name.trim()) {
    formError.value = 'Tên room không được để trống'
    return
  }

  if (!form.value.ownerId) {
    formError.value = 'Vui lòng chọn owner cho room'
    return
  }

  isSubmitting.value = true

  try {
    await roomService.createRoom(form.value)
    open.value = false
    emit('created')
  }
  catch (error: any) {
    formError.value = error?.response?.data || 'Có lỗi xảy ra, vui lòng thử lại'
  }
  finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <Dialog v-model:open="open">
    <DialogContent class="max-w-[520px]">
      <DialogHeader>
        <DialogTitle>
          Tạo Room mới
        </DialogTitle>
        <DialogDescription class="sr-only">
          Form tạo room mới
        </DialogDescription>
      </DialogHeader>

      <div class="flex flex-col gap-4 py-2">
        <!-- Name -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Tên Room <span class="text-red-500">*</span>
          </label>
          <UiInput
            v-model="form.name"
            placeholder="VD: Team Marketing"
          />
        </div>

        <!-- Description -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Description
          </label>
          <UiTextarea
            v-model="form.description"
            rows="3"
            placeholder="Mô tả ngắn về room này..."
          />
        </div>

        <!-- Owner -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Owner <span class="text-red-500">*</span>
          </label>

          <div
            v-if="selectedOwner"
            class="flex items-center justify-between rounded-lg border border-border bg-muted/40 px-3 py-2"
          >
            <div>
              <p class="text-[13px] font-medium">
                {{ selectedOwner.username }}
              </p>
              <p v-if="selectedOwner.email" class="text-[11px] text-muted-foreground">
                {{ selectedOwner.email }}
              </p>
            </div>
            <UiButton variant="ghost" size="sm" @click="clearOwner">
              Đổi
            </UiButton>
          </div>

          <div v-else class="relative">
            <UiInput
              v-model="ownerKeyword"
              placeholder="Tìm theo username hoặc email..."
              @focus="showOwnerDropdown = true"
            />

            <div
              v-if="showOwnerDropdown && (ownerOptions.length || isSearchingOwner)"
              class="absolute z-10 mt-1 w-full rounded-lg border border-border bg-background shadow-md"
            >
              <p v-if="isSearchingOwner" class="px-3 py-2 text-[12px] text-muted-foreground">
                Đang tìm...
              </p>

              <button
                v-for="user in ownerOptions"
                :key="user.id"
                type="button"
                class="flex w-full flex-col items-start px-3 py-2 text-left hover:bg-muted/60"
                @click="pickOwner(user)"
              >
                <span class="text-[13px] font-medium">{{ user.username }}</span>
                <span class="text-[11px] text-muted-foreground">{{ user.email }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Status -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Trạng thái
          </label>
          <Select v-model="form.status">
            <SelectTrigger class="h-9 w-full">
              <SelectValue placeholder="Trạng thái" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="OPEN">
                Đang mở
              </SelectItem>
              <SelectItem value="LOCKED">
                Đã khoá
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <p v-if="formError" class="text-[12px] text-red-500">
          {{ formError }}
        </p>
      </div>

      <DialogFooter>
        <UiButton variant="outline" :disabled="isSubmitting" @click="open = false">
          Hủy
        </UiButton>
        <UiButton :disabled="isSubmitting" @click="handleSubmitCreate">
          {{ isSubmitting ? 'Đang lưu...' : 'Lưu' }}
        </UiButton>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
