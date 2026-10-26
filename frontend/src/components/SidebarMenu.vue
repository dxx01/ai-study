<script setup lang="ts">
import type { Component } from 'vue'
import * as Icons from '@element-plus/icons-vue'
import type { MenuItem } from '@/types/api'

defineOptions({ name: 'SidebarMenu' })
defineProps<{ items: MenuItem[] }>()

const iconMap = Icons as unknown as Record<string, Component>

function iconOf(name?: string | null): Component {
  return (name && iconMap[name]) || Icons.Menu
}
</script>

<template>
  <template v-for="item in items" :key="item.path">
    <el-sub-menu v-if="item.children?.length" :index="item.path">
      <template #title>
        <el-icon><component :is="iconOf(item.icon)" /></el-icon>
        <span>{{ item.name }}</span>
      </template>
      <SidebarMenu :items="item.children" />
    </el-sub-menu>
    <el-menu-item v-else :index="item.path">
      <el-icon><component :is="iconOf(item.icon)" /></el-icon>
      <span>{{ item.name }}</span>
    </el-menu-item>
  </template>
</template>
