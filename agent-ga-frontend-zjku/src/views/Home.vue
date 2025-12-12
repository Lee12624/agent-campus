<template>
  <div class="home">
    <!-- 卡通背景装饰 -->
    <div class="background-decoration">
      <div class="cartoon-cloud cloud-1">☁️</div>
      <div class="cartoon-cloud cloud-2">☁️</div>
      <div class="cartoon-cloud cloud-3">☁️</div>
      <div class="cartoon-star star-1">⭐</div>
      <div class="cartoon-star star-2">⭐</div>
      <div class="cartoon-star star-3">⭐</div>
      <div class="floating-hearts">
        <div class="heart heart-1">💖</div>
        <div class="heart heart-2">💕</div>
        <div class="heart heart-3">💗</div>
      </div>
    </div>

    <div class="content">
      <!-- 卡通头部区域 -->
      <div class="cartoon-header">
        <!-- 学校Logo卡片 -->
        <div class="logo-card">
          <div class="card-decoration top-left"></div>
          <div class="card-decoration top-right"></div>
          <div class="card-decoration bottom-left"></div>
          <div class="card-decoration bottom-right"></div>
          <img src="/assets/images/zjklogo.jpg" alt="张家口学院Logo" class="school-logo-image" />
        </div>

        <!-- 卡通角色展示 -->
        <div class="characters-showcase">
          <div class="character-card">
            <div class="character-avatar">🧠</div>
            <div class="character-name">智能大脑</div>
          </div>
          <div class="character-card">
            <div class="character-avatar">🤖</div>
            <div class="character-name">AI助手</div>
          </div>
          <div class="character-card">
            <div class="character-avatar">💬</div>
            <div class="character-name">贴心对话</div>
          </div>
        </div>
      </div>

      <!-- 卡通标题区域 -->
      <div class="title-section">
        <div class="title-bubble">
          <h1 class="main-title">
            <span class="title-text">张家口学院明湖校区</span>
            <span class="title-text">校园智能问答助手</span>
          </h1>
          <div class="bubble-tail"></div>
        </div>
        <p class="subtitle">您的专属校园AI助手，为您解答各类校园问题</p>
      </div>

      <!-- 卡通功能区域 -->
      <div class="features-section">
        <div class="feature-grid">
          <div class="feature-card">
            <div class="card-icon">📚</div>
            <h3>课程信息查询</h3>
            <p>快速查询课程安排、教师信息等</p>
          </div>
          <div class="feature-card">
            <div class="card-icon">🏫</div>
            <h3>校园服务指南</h3>
            <p>宿舍、食堂、图书馆等服务查询</p>
          </div>
          <div class="feature-card">
            <div class="card-icon">🤝</div>
            <h3>智能问答</h3>
            <p>AI智能解答各类校园问题</p>
          </div>
        </div>
      </div>

      <!-- 卡通开始按钮 -->
      <div class="action-section">
        <button class="start-chat-btn cartoon-btn" @click="goToChat">
          <div class="btn-sparkle sparkle-1">✨</div>
          <div class="btn-sparkle sparkle-2">✨</div>
          <span class="btn-text">开始对话</span>
          <div class="btn-arrow">🚀</div>
        </button>
      </div>

      <!-- 版权信息卡片 -->
      <div class="copyright-card">
        <div class="copyright-content">
          <span class="copyright-text">
            © 张家口学院 | 主理人：Lee | 管理员微信：Lee12624 | 2026
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToChat = () => {
  router.push('/chat')
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 25%, #fecfef 50%, #a8edea 75%, #fed6e3 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 卡通背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.cartoon-cloud {
  position: absolute;
  font-size: 3rem;
  animation: cloudFloat 8s ease-in-out infinite;
  opacity: 0.6;
}

.cloud-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.cloud-2 {
  top: 20%;
  right: 15%;
  animation-delay: 3s;
}

.cloud-3 {
  bottom: 25%;
  left: 20%;
  animation-delay: 6s;
}

.cartoon-star {
  position: absolute;
  font-size: 2rem;
  animation: starTwinkle 3s ease-in-out infinite;
}

.star-1 {
  top: 15%;
  right: 25%;
  animation-delay: 0s;
}

.star-2 {
  top: 35%;
  left: 15%;
  animation-delay: 1s;
}

.star-3 {
  bottom: 30%;
  right: 20%;
  animation-delay: 2s;
}

.floating-hearts {
  position: absolute;
  width: 100%;
  height: 100%;
}

.heart {
  position: absolute;
  font-size: 1.5rem;
  animation: heartFloat 6s ease-in-out infinite;
}

.heart-1 {
  top: 20%;
  left: 30%;
  animation-delay: 0s;
}

.heart-2 {
  top: 50%;
  right: 35%;
  animation-delay: 2s;
}

.heart-3 {
  bottom: 40%;
  left: 35%;
  animation-delay: 4s;
}

@keyframes cloudFloat {
  0%, 100% {
    transform: translateY(0px) translateX(0px);
  }
  25% {
    transform: translateY(-10px) translateX(10px);
  }
  50% {
    transform: translateY(0px) translateX(20px);
  }
  75% {
    transform: translateY(-5px) translateX(10px);
  }
}

@keyframes starTwinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

@keyframes heartFloat {
  0%, 100% {
    transform: translateY(0px);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-20px);
    opacity: 0.8;
  }
}

.content {
  text-align: center;
  color: #333;
  z-index: 1;
  position: relative;
  padding: 2rem;
  max-width: 1000px;
  width: 100%;
}

/* 卡通头部区域 */
.cartoon-header {
  margin-bottom: 3rem;
}

/* Logo卡片 */
.logo-card {
  position: relative;
  display: inline-block;
  margin-bottom: 2rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 3px solid transparent;
  background-image: linear-gradient(white, white), linear-gradient(45deg, #ff9a9e, #fecfef);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  animation: cardPulse 3s ease-in-out infinite;
}

.card-decoration {
  position: absolute;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: #ff6b6b;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.top-left {
  top: -5px;
  left: -5px;
}

.top-right {
  top: -5px;
  right: -5px;
}

.bottom-left {
  bottom: -5px;
  left: -5px;
}

.bottom-right {
  bottom: -5px;
  right: -5px;
}

.school-logo-image {
  width: 120px;
  height: 120px;
  border-radius: 20px;
  border: 3px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.school-logo-image:hover {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

@keyframes cardPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
}

/* 角色展示 */
.characters-showcase {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 2rem;
}

.character-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.9);
  padding: 1.5rem 1rem;
  border-radius: 20px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 107, 107, 0.2);
  transition: all 0.3s ease;
  animation: characterBounce 4s ease-in-out infinite;
}

.character-card:nth-child(1) {
  animation-delay: 0s;
}

.character-card:nth-child(2) {
  animation-delay: 0.5s;
}

.character-card:nth-child(3) {
  animation-delay: 1s;
}

.character-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.character-avatar {
  font-size: 3rem;
  animation: avatarPulse 2s ease-in-out infinite;
}

.character-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
  text-align: center;
}

@keyframes characterBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

@keyframes avatarPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* 标题区域 */
.title-section {
  margin-bottom: 3rem;
}

.title-bubble {
  position: relative;
  display: inline-block;
  background: rgba(255, 255, 255, 0.95);
  padding: 2rem 3rem;
  border-radius: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 3px solid rgba(255, 107, 107, 0.2);
  animation: bubbleFloat 4s ease-in-out infinite;
}

.main-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin: 0;
  line-height: 1.3;
  color: #333;
}

.title-text {
  display: block;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: textGlow 3s ease-in-out infinite;
}

.bubble-tail {
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 15px solid transparent;
  border-right: 15px solid transparent;
  border-top: 15px solid rgba(255, 107, 107, 0.2);
}

.bubble-tail::after {
  content: '';
  position: absolute;
  top: 3px;
  left: -12px;
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-top: 12px solid rgba(255, 255, 255, 0.95);
}

.subtitle {
  font-size: 1.1rem;
  color: #666;
  margin-top: 1.5rem;
  font-weight: 500;
}

@keyframes bubbleFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes textGlow {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.2);
  }
}

/* 功能区域 */
.features-section {
  margin-bottom: 3rem;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 2rem 1.5rem;
  border-radius: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 107, 107, 0.1);
  text-align: center;
  transition: all 0.3s ease;
  animation: cardSlideIn 0.6s ease-out;
  animation-fill-mode: both;
}

.feature-card:nth-child(1) {
  animation-delay: 0.1s;
}

.feature-card:nth-child(2) {
  animation-delay: 0.2s;
}

.feature-card:nth-child(3) {
  animation-delay: 0.3s;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 107, 107, 0.3);
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  animation: iconBounce 2s ease-in-out infinite;
}

.feature-card h3 {
  color: #333;
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.feature-card p {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
}

@keyframes cardSlideIn {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes iconBounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* 按钮区域 */
.action-section {
  margin-bottom: 3rem;
}

.start-chat-btn {
  position: relative;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  border: none;
  color: white;
  padding: 1.2rem 3rem;
  font-size: 1.3rem;
  font-weight: bold;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
  overflow: hidden;
  animation: btnPulse 2s ease-in-out infinite;
}

.start-chat-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.start-chat-btn:hover::before {
  left: 100%;
}

.start-chat-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 15px 35px rgba(255, 107, 107, 0.4);
}

.btn-sparkle {
  position: absolute;
  font-size: 1rem;
  animation: sparkle 2s ease-in-out infinite;
}

.sparkle-1 {
  top: 10px;
  left: 20px;
  animation-delay: 0s;
}

.sparkle-2 {
  top: 15px;
  right: 25px;
  animation-delay: 1s;
}

.btn-text {
  position: relative;
  z-index: 2;
}

.btn-arrow {
  position: relative;
  z-index: 2;
  font-size: 1.5rem;
  animation: arrowBounce 1s ease-in-out infinite;
}

@keyframes btnPulse {
  0%, 100% {
    box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
  }
  50% {
    box-shadow: 0 12px 35px rgba(255, 107, 107, 0.5);
  }
}

@keyframes sparkle {
  0%, 100% {
    opacity: 0;
    transform: scale(0.5);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes arrowBounce {
  0%, 100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(5px);
  }
}

/* 平板端响应式设计 (768px - 1024px) */
@media (min-width: 769px) and (max-width: 1024px) {
  .main-title {
    font-size: 2.2rem;
  }

  .title-bubble {
    padding: 1.8rem 2.5rem;
  }

  .school-logo-image {
    width: 100px;
    height: 100px;
  }

  .characters-showcase {
    gap: 1.5rem;
  }

  .character-card {
    padding: 1.2rem 0.8rem;
  }

  .character-avatar {
    font-size: 2.5rem;
  }

  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
  }

  .feature-card {
    padding: 1.5rem 1rem;
  }

  .card-icon {
    font-size: 2.5rem;
  }

  .start-chat-btn {
    padding: 1rem 2.5rem;
    font-size: 1.2rem;
  }

  .content {
    padding: 1.5rem;
  }
}

/* 移动端响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
  }

  .title-bubble {
    padding: 1.5rem 2rem;
  }

  .school-logo-image {
    width: 80px;
    height: 80px;
  }

  .characters-showcase {
    gap: 1rem;
  }

  .character-card {
    padding: 1rem 0.8rem;
  }

  .character-avatar {
    font-size: 2.5rem;
  }

  .feature-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .feature-card {
    padding: 1.5rem 1rem;
  }

  .card-icon {
    font-size: 2.5rem;
  }

  .start-chat-btn {
    padding: 1rem 2rem;
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .content {
    padding: 1rem;
  }

  .main-title {
    font-size: 1.8rem;
  }

  .title-bubble {
    padding: 1.2rem 1.5rem;
  }

  .subtitle {
    font-size: 1rem;
    margin-top: 1rem;
  }

  .school-logo-image {
    width: 70px;
    height: 70px;
  }

  .characters-showcase {
    flex-direction: column;
    gap: 0.8rem;
  }

  .character-card {
    width: 100%;
    max-width: 200px;
    padding: 0.8rem 0.6rem;
  }

  .character-avatar {
    font-size: 2rem;
  }

  .character-name {
    font-size: 0.8rem;
  }

  .feature-card {
    padding: 1.2rem 0.8rem;
  }

  .card-icon {
    font-size: 2rem;
  }

  .feature-card h3 {
    font-size: 1.1rem;
  }

  .feature-card p {
    font-size: 0.9rem;
  }

  .start-chat-btn {
    padding: 0.9rem 1.8rem;
    font-size: 1rem;
  }

  .btn-arrow {
    font-size: 1.2rem;
  }

  .copyright-card {
    margin-top: 1.5rem;
  }

  .copyright-content {
    padding: 0.6rem 1rem;
  }

  .copyright-text {
    font-size: 0.75rem;
  }
}

/* 版权信息卡片 */
.copyright-card {
  margin-top: 2rem;
  padding: 1rem;
}

.copyright-content {
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem 2rem;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 107, 107, 0.1);
  animation: copyrightFadeIn 1s ease-out;
}

.copyright-text {
  color: #666;
  font-size: 0.9rem;
  font-weight: 500;
  margin: 0;
}

@keyframes copyrightFadeIn {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端版权卡片适配 */
@media (max-width: 768px) {
  .copyright-card {
    margin-top: 1.5rem;
  }

  .copyright-content {
    padding: 0.8rem 1rem;
  }

  .copyright-text {
    font-size: 0.8rem;
  }
}
</style>


