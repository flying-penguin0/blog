<template>
  <div class="about-page">
    <Header />
    <div class="about-container">
      <div class="about-layout">
        <!-- Left Sidebar -->
        <aside class="about-sidebar">
          <div class="sidebar-content">
            <div class="logo-section">
              <div class="robot-emoji">🤖</div>
              <h2 class="sidebar-title">AI 知识博客</h2>
              <p class="sidebar-subtitle">DeepSeek 驱动</p>
            </div>
            
            <nav class="sidebar-nav">
              <a href="#intro" class="nav-link" :class="{ active: activeSection === 'intro' }">
                <span class="nav-icon">💡</span>
                <span>博客简介</span>
              </a>
              <a href="#tech" class="nav-link" :class="{ active: activeSection === 'tech' }">
                <span class="nav-icon">⚙️</span>
                <span>技术栈</span>
              </a>
              <a href="#features" class="nav-link" :class="{ active: activeSection === 'features' }">
                <span class="nav-icon">✨</span>
                <span>核心功能</span>
              </a>
              <a href="#links" class="nav-link" :class="{ active: activeSection === 'links' }">
                <span class="nav-icon">🔗</span>
                <span>开源地址</span>
              </a>
            </nav>
          </div>
        </aside>

        <!-- Right Content -->
        <main class="about-content">
          <!-- Introduction Section -->
          <section id="intro" class="content-section">
            <div class="section-header">
              <div class="section-icon">💡</div>
              <h2 class="section-title">博客简介</h2>
            </div>
            <div class="intro-card">
              <p class="intro-text">
                这是一个基于 <strong>DeepSeek 开放平台</strong>的个人知识博客系统。通过该系统，用户可以借助 DeepSeek 大模型更好地管理和整理自己所学的知识。
              </p>
              <p class="intro-text">
                与此同时，知识的价值不仅在于自我积累，更在于分享与交流。通过人工智能驱动的知识分享平台，个人可以将整理好的内容高效传播给他人，实现知识的再利用与价值放大。
              </p>
            </div>
          </section>

          <!-- Tech Stack Section -->
          <section id="tech" class="content-section">
            <div class="section-header">
              <div class="section-icon">⚙️</div>
              <h2 class="section-title">技术栈</h2>
            </div>
            <div class="tech-grid">
              <!-- Frontend -->
              <div class="tech-card">
                <div class="tech-header">
                  <div class="tech-icon">🎨</div>
                  <h3>前端技术</h3>
                </div>
                <ul class="tech-list">
                  <li><span class="tech-name">Vue 3</span><span class="tech-desc">渐进式框架</span></li>
                  <li><span class="tech-name">Element Plus</span><span class="tech-desc">组件库</span></li>
                  <li><span class="tech-name">Vite</span><span class="tech-desc">构建工具</span></li>
                  <li><span class="tech-name">Pinia</span><span class="tech-desc">状态管理</span></li>
                  <li><span class="tech-name">Vue Router</span><span class="tech-desc">路由管理</span></li>
                  <li><span class="tech-name">Axios</span><span class="tech-desc">HTTP 客户端</span></li>
                  <li><span class="tech-name">md-editor-v3</span><span class="tech-desc">Markdown 编辑器</span></li>
                </ul>
              </div>

              <!-- Backend -->
              <div class="tech-card">
                <div class="tech-header">
                  <div class="tech-icon">⚙️</div>
                  <h3>后端技术</h3>
                </div>
                <ul class="tech-list">
                  <li><span class="tech-name">Spring Boot</span><span class="tech-desc">开发框架</span></li>
                  <li><span class="tech-name">MyBatis Plus</span><span class="tech-desc">持久层框架</span></li>
                  <li><span class="tech-name">MySQL</span><span class="tech-desc">关系型数据库</span></li>
                  <li><span class="tech-name">Redis</span><span class="tech-desc">缓存数据库</span></li>
                  <li><span class="tech-name">MinIO</span><span class="tech-desc">对象存储</span></li>
                  <li><span class="tech-name">JWT</span><span class="tech-desc">身份认证</span></li>
                  <li><span class="tech-name">WebSocket</span><span class="tech-desc">实时通信</span></li>
                </ul>
              </div>

              <!-- AI -->
              <div class="tech-card highlight">
                <div class="tech-header">
                  <div class="tech-icon">🤖</div>
                  <h3>AI 技术</h3>
                </div>
                <ul class="tech-list">
                  <li><span class="tech-name">DeepSeek API</span><span class="tech-desc">大语言模型</span></li>
                  <li><span class="tech-name">流式响应</span><span class="tech-desc">实时对话</span></li>
                  <li><span class="tech-name">智能问答</span><span class="tech-desc">内容理解</span></li>
                </ul>
              </div>
            </div>
          </section>

          <!-- Features Section -->
          <section id="features" class="content-section">
            <div class="section-header">
              <div class="section-icon">✨</div>
              <h2 class="section-title">核心功能</h2>
            </div>
            <div class="features-grid">
              <div class="feature-card">
                <div class="feature-icon">🤖</div>
                <h3 class="feature-title">AI 智能助手</h3>
                <p class="feature-desc">基于 DeepSeek 的文章问答</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">💬</div>
                <h3 class="feature-title">聊天室</h3>
                <p class="feature-desc">WebSocket 实时在线聊天</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">📝</div>
                <h3 class="feature-title">留言板</h3>
                <p class="feature-desc">访客留言与互动交流</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">💭</div>
                <h3 class="feature-title">评论交流</h3>
                <p class="feature-desc">评论点赞、敏感词过滤</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">✍️</div>
                <h3 class="feature-title">文章管理</h3>
                <p class="feature-desc">Markdown 编辑与发布</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">🏷️</div>
                <h3 class="feature-title">分类标签</h3>
                <p class="feature-desc">灵活的分类和标签系统</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">📢</div>
                <h3 class="feature-title">公告通知</h3>
                <p class="feature-desc">系统公告发布与管理</p>
              </div>
              
              <div class="feature-card">
                <div class="feature-icon">📊</div>
                <h3 class="feature-title">数据统计</h3>
                <p class="feature-desc">可视化数据展示</p>
              </div>
            </div>
          </section>

          <!-- Links Section -->
          <section id="links" class="content-section">
            <div class="section-header">
              <div class="section-icon">🔗</div>
              <h2 class="section-title">开源地址</h2>
            </div>
            <div class="links-grid">
              <a href="https://github.com/flying-penguin0" target="_blank" class="link-card github">
                <svg class="link-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M512 12.64c-282.752 0-512 229.216-512 512 0 226.208 146.688 418.144 350.08 485.632 25.6 4.736 35.008-11.104 35.008-24.64 0-12.192-0.48-52.544-0.704-95.328-142.464 30.976-172.512-60.416-172.512-60.416-23.296-59.168-56.832-74.912-56.832-74.912-46.464-31.776 3.52-31.136 3.52-31.136 51.392 3.616 78.464 52.768 78.464 52.768 45.664 78.272 119.776 55.648 148.992 42.56 4.576-33.088 17.856-55.68 32.512-68.48-113.728-12.928-233.28-56.864-233.28-253.024 0-55.904 20-101.568 52.768-137.44-5.312-12.896-22.848-64.96 4.96-135.488 0 0 43.008-13.76 140.832 52.48 40.832-11.36 84.64-17.024 128.16-17.248 43.488 0.192 87.328 5.888 128.256 17.248 97.728-66.24 140.64-52.48 140.64-52.48 27.872 70.528 10.336 122.592 5.024 135.488 32.832 35.84 52.704 81.536 52.704 137.44 0 196.64-119.776 239.936-233.792 252.64 18.368 15.904 34.72 47.04 34.72 94.816 0 68.512-0.608 123.648-0.608 140.512 0 13.632 9.216 29.6 35.168 24.576 203.328-67.776 349.856-259.616 349.856-485.76 0-282.784-229.248-512-512-512z"/>
                </svg>
                <div class="link-content">
                  <h3>GitHub</h3>
                  <p>fly-penguin0</p>
                </div>
              </a>
              
              <a href="https://gitee.com/fly-penguin" target="_blank" class="link-card gitee">
                <svg class="link-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M512 1024C229.222 1024 0 794.778 0 512S229.222 0 512 0s512 229.222 512 512-229.222 512-512 512z m259.149-568.883h-290.74a25.293 25.293 0 0 0-25.292 25.293l-0.026 63.206c0 13.952 11.315 25.293 25.267 25.293h177.024c13.978 0 25.293 11.315 25.293 25.267v12.646a75.853 75.853 0 0 1-75.853 75.853h-240.23a25.293 25.293 0 0 1-25.267-25.293V417.203a75.853 75.853 0 0 1 75.827-75.853h353.946a25.293 25.293 0 0 0 25.267-25.292l0.077-63.207a25.293 25.293 0 0 0-25.268-25.293H417.152a189.62 189.62 0 0 0-189.62 189.645V771.15c0 13.977 11.316 25.293 25.294 25.293h372.94a170.65 170.65 0 0 0 170.65-170.65V480.384a25.293 25.293 0 0 0-25.293-25.267z"/>
                </svg>
                <div class="link-content">
                  <h3>Gitee</h3>
                  <p>fly-penguin</p>
                </div>
              </a>
            </div>
          </section>

        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Header from '@/components/Header.vue'

const activeSection = ref('intro')

const handleScroll = () => {
  const sections = ['intro', 'tech', 'features', 'links']
  const scrollPosition = window.scrollY + 150

  for (const section of sections) {
    const element = document.getElementById(section)
    if (element) {
      const offsetTop = element.offsetTop
      const offsetBottom = offsetTop + element.offsetHeight
      
      if (scrollPosition >= offsetTop && scrollPosition < offsetBottom) {
        activeSection.value = section
        break
      }
    }
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
.about-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.about-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.about-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

// Left Sidebar
.about-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  
  .sidebar-content {
    background: white;
    border-radius: 16px;
    padding: 30px 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    
    .logo-section {
      text-align: center;
      padding-bottom: 24px;
      border-bottom: 1px solid #e4e7ed;
      margin-bottom: 24px;
      
      .deepseek-logo {
        width: 80px;
        height: 80px;
        border-radius: 20px;
        padding: 12px;
        margin-bottom: 16px;
      }
      
      .robot-emoji {
        font-size: 64px;
        line-height: 1;
        margin-bottom: 16px;
      }
      
      .sidebar-title {
        font-size: 20px;
        font-weight: 700;
        color: #303133;
        margin: 0 0 6px;
      }
      
      .sidebar-subtitle {
        font-size: 13px;
        color: #909399;
        margin: 0;
      }
    }
    
    .sidebar-nav {
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .nav-link {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        border-radius: 10px;
        text-decoration: none;
        color: #606266;
        font-size: 15px;
        font-weight: 500;
        transition: all 0.3s;
        
        .nav-icon {
          font-size: 20px;
        }
        
        &:hover {
          background: #f5f7fa;
          color: #409eff;
        }
        
        &.active {
          background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
          color: white;
        }
      }
    }
  }
}

// Right Content
.about-content {
  flex: 1;
  min-width: 0;
}

.content-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 2px solid #f0f0f0;
    
    .section-icon {
      font-size: 32px;
    }
    
    .section-title {
      font-size: 26px;
      font-weight: 700;
      color: #303133;
      margin: 0;
    }
  }
}

// Introduction
.intro-card {
  .intro-text {
    font-size: 15px;
    line-height: 1.8;
    color: #606266;
    margin: 0 0 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    strong {
      color: #409eff;
      font-weight: 600;
    }
  }
}

// Tech Stack
.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  
  .tech-card {
    background: #f8f9fa;
    border-radius: 12px;
    padding: 24px;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    &.highlight {
      background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
      
      .tech-header h3 {
        color: white;
      }
      
      .tech-list li {
        .tech-name,
        .tech-desc {
          color: rgba(255, 255, 255, 0.95);
        }
        
        .tech-desc {
          opacity: 0.8;
        }
      }
    }
    
    .tech-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 16px;
      
      .tech-icon {
        font-size: 24px;
      }
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }
    }
    
    .tech-list {
      list-style: none;
      padding: 0;
      margin: 0;
      
      li {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;
        border-bottom: 1px solid rgba(0, 0, 0, 0.05);
        
        &:last-child {
          border-bottom: none;
        }
        
        .tech-name {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
        
        .tech-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

// Features
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  
  .feature-card {
    background: #f8f9fa;
    border-radius: 12px;
    padding: 20px;
    text-align: center;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
      background: linear-gradient(135deg, rgba(102, 177, 255, 0.05) 0%, rgba(64, 158, 255, 0.05) 100%);
      
      .feature-icon {
        transform: scale(1.15);
      }
    }
    
    .feature-icon {
      font-size: 36px;
      margin-bottom: 10px;
      display: inline-block;
      transition: all 0.3s;
    }
    
    .feature-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px;
    }
    
    .feature-desc {
      font-size: 13px;
      color: #909399;
      line-height: 1.5;
      margin: 0;
    }
  }
}

// Links
.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  
  .link-card {
    display: flex;
    align-items: center;
    gap: 16px;
    background: #f8f9fa;
    border-radius: 12px;
    padding: 24px;
    text-decoration: none;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    .link-icon {
      width: 48px;
      height: 48px;
      flex-shrink: 0;
    }
    
    .link-content {
      h3 {
        font-size: 18px;
        font-weight: 600;
        margin: 0 0 4px;
      }
      
      p {
        font-size: 13px;
        margin: 0;
        opacity: 0.7;
      }
    }
    
    &.github {
      .link-icon {
        fill: #24292e;
      }
      
      .link-content h3 {
        color: #24292e;
      }
      
      .link-content p {
        color: #586069;
      }
    }
    
    &.gitee {
      .link-icon {
        fill: #c71d23;
      }
      
      .link-content h3 {
        color: #c71d23;
      }
      
      .link-content p {
        color: #c71d23;
      }
    }
  }
}

// Footer
.about-footer {
  text-align: center;
  padding: 24px;
  margin-top: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  p {
    font-size: 15px;
    color: #606266;
    margin: 0 0 6px;
  }
  
  .footer-small {
    font-size: 13px;
    color: #909399;
  }
}

// Responsive
@media (max-width: 1024px) {
  .about-layout {
    flex-direction: column;
  }
  
  .about-sidebar {
    width: 100%;
    position: static;
    
    .sidebar-content {
      .sidebar-nav {
        flex-direction: row;
        flex-wrap: wrap;
        
        .nav-link {
          flex: 1;
          min-width: 120px;
          justify-content: center;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .about-container {
    padding: 15px;
  }
  
  .content-section {
    padding: 24px 20px;
    
    .section-header {
      .section-icon {
        font-size: 28px;
      }
      
      .section-title {
        font-size: 22px;
      }
    }
  }
  
  .tech-grid,
  .features-grid,
  .links-grid {
    grid-template-columns: 1fr;
  }
  
  .sidebar-content {
    .logo-section .deepseek-logo {
      width: 60px;
      height: 60px;
    }
    
    .logo-section .robot-emoji {
      font-size: 52px;
    }
  }
}
</style>
