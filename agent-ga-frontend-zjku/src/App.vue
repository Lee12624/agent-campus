<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', 'Hiragino Sans GB', sans-serif;
  background: linear-gradient(165deg, #e8eef9 0%, #eef2f7 45%, #f8fafc 100%);
  background-attachment: fixed;
  color: #0f172a;
  -webkit-font-smoothing: antialiased;
}

#app {
  height: 100vh;
  position: relative;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #c5c9d1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8adb8;
}
</style>
