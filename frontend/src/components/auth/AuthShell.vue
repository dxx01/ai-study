<script setup lang="ts">
defineProps<{
  title: string
  subtitle: string
  variant?: 'teal' | 'blue'
}>()
</script>

<template>
  <div class="auth-page" :class="variant === 'blue' ? 'auth-page--blue' : 'auth-page--teal'">
    <el-card class="auth-card" shadow="hover">
      <h1 class="auth-title">{{ title }}</h1>
      <p class="auth-subtitle">{{ subtitle }}</p>
      <slot />
      <p v-if="$slots.footer" class="auth-footer">
        <slot name="footer" />
      </p>
    </el-card>
  </div>
</template>
