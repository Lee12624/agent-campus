<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// App component
</script>

<style>
/* 全局卡通风格样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: 'Comic Neue', 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 25%, #fecfef 50%, #a8edea 75%, #fed6e3 100%);
  color: #333;
}

#app {
  height: 100vh;
  position: relative;
}

/* 卡通风格按钮基础样式 */
.cartoon-btn {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  border: none;
  color: white;
  padding: 0.8rem 1.5rem;
  font-size: 1rem;
  font-weight: bold;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
  font-family: inherit;
  position: relative;
  overflow: hidden;
}

.cartoon-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.cartoon-btn:hover::before {
  left: 100%;
}

.cartoon-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.4);
}

.cartoon-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

/* 卡通风格卡片 */
.cartoon-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
  background-image: linear-gradient(white, white), linear-gradient(45deg, #ff9a9e, #fecfef);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  transition: all 0.3s ease;
}

.cartoon-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

/* 卡通风格输入框 */
.cartoon-input {
  border: 2px solid #ddd;
  border-radius: 15px;
  padding: 0.8rem 1rem;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.cartoon-input:focus {
  outline: none;
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
  background: white;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(45deg, #ff9a9e, #fecfef);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
}

/* 响应式字体大小和布局 */
@media (min-width: 1025px) {
  /* PC端大屏幕 */
  html {
    font-size: 16px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  /* 平板端 */
  html {
    font-size: 15px;
  }

  #app {
    padding: 0 1rem;
  }
}

@media (max-width: 768px) {
  /* 手机端 */
  html {
    font-size: 14px;
  }

  #app {
    padding: 0 0.5rem;
  }
}

@media (max-width: 480px) {
  /* 小屏手机端 */
  html {
    font-size: 12px;
  }

  #app {
    padding: 0 0.25rem;
  }
}

/* 动画工具类 */
@keyframes cartoon-bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes cartoon-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes cartoon-float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-bounce {
  animation: cartoon-bounce 2s ease-in-out infinite;
}

.animate-pulse {
  animation: cartoon-pulse 2s ease-in-out infinite;
}

.animate-float {
  animation: cartoon-float 3s ease-in-out infinite;
}
</style>

