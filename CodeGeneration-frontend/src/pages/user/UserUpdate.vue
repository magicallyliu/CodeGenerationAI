<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      修改个人信息
    </h2>
    <!--    <a-typography-paragraph v-if="spaceId" type="secondary">-->
    <!--      保存至空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>-->
    <!--    </a-typography-paragraph>-->
    <!-- 图片信息表单 -->
    <a-form

      :model="userForm"
      @finish="handleSubmit"
    >
      <a-form-item name="name" label="昵称">
        <a-input v-model:value="userForm.userName" placeholder="请输入昵称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea
          v-model:value="userForm.userProfile"
          placeholder="请输入简介"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">上传</a-button>
      </a-form-item>
    </a-form>

  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserVoById, updateUser } from '@/api/userController.ts'


const router = useRouter()
const route = useRoute()

const user = ref<API.UserVO>()
const userForm = reactive<API.UserUpdateRequest>({})

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId
})


/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.UserVO) => {
  user.value = newPicture
  userForm.userName = newPicture.userName
}


/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  console.log(values)
  const userId = user.value.id
  if (!userId) {
    return
  }
  const res = await updateUser({
    id: userId,
    ...values
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('更新成功')
    // 跳转到图片详情页
    // router.push({
    //   path: `/user/${userId}`
    // })
  } else {
    message.error('更新失败，' + res.data.message)
  }
}

// 获取老数据
const getOldPicture = async () => {
  // 获取数据
  const id = route.query?.id
  if (id) {
    const res = await getUserVoById({
      id: id
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      // user.value = data
      userForm.userName = data.userName
      userForm.userAvatar=data.userAvatar
      userForm.userProfile = data.userProfile
    }
  }
}

onMounted(() => {
  getOldPicture()
})


</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}

#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}
</style>
