/**
 * AI API
 * 注意：AI 接口使用 SSE（Server-Sent Events）流式响应
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

/**
 * 创建 SSE 连接
 * @param {string} url - 接口 URL
 * @param {object} data - 请求数据
 * @param {function} onMessage - 消息回调
 * @param {function} onError - 错误回调
 * @param {function} onComplete - 完成回调
 */
const createSSEConnection = (url, data, onMessage, onError, onComplete) => {
  const token = localStorage.getItem('token')
  
  fetch(`${API_BASE_URL}${url}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : '',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(data)
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      
      const readStream = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            console.log('Stream complete')
            // 处理缓冲区中剩余的数据
            if (buffer.trim()) {
              processBuffer(buffer)
            }
            onComplete && onComplete()
            return
          }
          
          // 解码数据块
          const chunk = decoder.decode(value, { stream: true })
          console.log('Received raw chunk:', chunk)
          
          // 将数据块添加到缓冲区
          buffer += chunk
          
          // 处理缓冲区中的数据
          // SSE 格式: data: content\n\n
          const lines = buffer.split('\n')
          
          // 保留最后一个不完整的行
          buffer = lines.pop() || ''
          
          // 处理完整的行
          lines.forEach(line => {
            if (line.startsWith('data:')) {
              // 移除 "data:" 前缀
              let content = line.substring(5)
              
              if (content && content !== '[DONE]') {
                // 处理转义字符
                content = unescapeContent(content)
                // 修复Markdown格式
                content = fixMarkdownFormat(content)
                onMessage && onMessage(content)
              }
            }
          })
          
          readStream()
        }).catch(error => {
          console.error('Stream read error:', error)
          onError && onError(error)
        })
      }
      
      const processBuffer = (buf) => {
        const lines = buf.split('\n')
        lines.forEach(line => {
          if (line.startsWith('data:')) {
            let content = line.substring(5)
            if (content && content !== '[DONE]') {
              content = unescapeContent(content)
              content = fixMarkdownFormat(content)
              onMessage && onMessage(content)
            }
          }
        })
      }
      
      readStream()
    })
    .catch(error => {
      console.error('Fetch error:', error)
      onError && onError(error)
    })
}

/**
 * 反转义内容
 */
const unescapeContent = (content) => {
  return content
    .replace(/\\n/g, '\n')
    .replace(/\\r/g, '\r')
    .replace(/\\t/g, '\t')
    .replace(/\\"/g, '"')
    .replace(/\\\\/g, '\\')
}

/**
 * 修复Markdown格式
 * 在标题符号后添加空格（如果没有的话）
 */
const fixMarkdownFormat = (content) => {
  // 修复标题格式：#标题 -> # 标题
  content = content.replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
  // 修复列表格式：-项目 -> - 项目
  content = content.replace(/^(-|\*|\+)([^\s])/gm, '$1 $2')
  // 修复有序列表格式：1.项目 -> 1. 项目
  content = content.replace(/^(\d+\.)([^\s])/gm, '$1 $2')
  
  // 修复粗体格式：** 文字 ** -> **文字**
  content = content.replace(/\*\* ([^*]+) \*\*/g, '**$1**')
  // 修复斜体格式：* 文字 * -> *文字*
  content = content.replace(/\* ([^*]+) \*/g, '*$1*')
  
  return content
}

/**
 * 生成文章内容
 */
export const generateContent = (outline, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/generate-content',
    { outline, message: outline },
    onMessage,
    onError,
    onComplete
  )
}

/**
 * AI 续写
 */
export const continueWriting = (content, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/continue-writing',
    { content, message: content },
    onMessage,
    onError,
    onComplete
  )
}

/**
 * 内容优化
 */
export const optimizeContent = (content, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/optimize',
    { content, message: content },
    onMessage,
    onError,
    onComplete
  )
}

/**
 * 文章问答
 */
export const articleQA = (articleContent, question, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/article-qa',
    { articleContent, message: question },
    onMessage,
    onError,
    onComplete
  )
}


/**
 * AI 提取摘要
 */
export const extractSummary = (content, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/extract-summary',
    { content, message: content },
    onMessage,
    onError,
    onComplete
  )
}
