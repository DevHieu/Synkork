<script setup lang="ts">
import { Plus, ChevronRight } from "lucide-vue-next";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
} from "@/components/ui/sidebar";

defineProps<{
  label: string;
  canManage: boolean;
}>();

const emit = defineEmits<{
  add: [];
}>();
</script>

<template>
  <Collapsible as-child default-open class="group/collapsible">
    <SidebarGroup>
      <SidebarGroupLabel as-child>
        <CollapsibleTrigger
          class="flex items-center justify-between w-full group/label text-muted-foreground hover:text-foreground transition-colors duration-200"
        >
          <div class="flex items-center">
            <ChevronRight
              class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
            />
            {{ label }}
          </div>
          <button
            v-if="canManage"
            @click.stop="emit('add')"
            class="opacity-0 group-hover/label:opacity-100 transition-opacity duration-200 hover:bg-muted rounded p-0.5"
          >
            <Plus class="h-3.5 w-3.5" />
          </button>
        </CollapsibleTrigger>
      </SidebarGroupLabel>
      <CollapsibleContent>
        <SidebarGroupContent>
          <SidebarMenu>
            <slot />
          </SidebarMenu>
        </SidebarGroupContent>
      </CollapsibleContent>
    </SidebarGroup>
  </Collapsible>
</template>
