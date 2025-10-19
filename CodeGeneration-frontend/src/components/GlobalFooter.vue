<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a>
          © 2025 幻化.All rights reserved
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px;
  margin-top: auto;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  flex-shrink: 0;
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
