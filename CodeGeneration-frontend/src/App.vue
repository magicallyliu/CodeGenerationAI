<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { healthCheck } from '@/api/healthController.ts'

healthCheck().then((res) => {
  console.log(res)
})

//获取当前登录用户信息
const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

</script>

<style scoped>

</style>
