/**
 * AI API
 * AI 接口使用 SSE（Server-Sent Events）流式响应
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

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
      let completed = false

      const finish = () => {
        if (completed) return
        completed = true
        onComplete && onComplete()
      }

      const processEventBlock = (block) => {
        const lines = block.split('\n')

        for (const rawLine of lines) {
          const line = rawLine.trimEnd()
          if (!line.startsWith('data:')) continue

          const payload = line.slice(5).trim()
          if (!payload) continue

          try {
            const event = JSON.parse(payload)
            if (event.done) {
              finish()
              return true
            }
            if (event.content) {
              onMessage && onMessage(event.content)
            }
          } catch (error) {
            console.error('Failed to parse SSE payload:', payload, error)
          }
        }

        return false
      }

      const readStream = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            if (buffer.trim()) {
              processEventBlock(buffer)
            }
            finish()
            return
          }

          buffer += decoder.decode(value, { stream: true })

          const blocks = buffer.split('\n\n')
          buffer = blocks.pop() || ''

          for (const block of blocks) {
            const shouldStop = processEventBlock(block)
            if (shouldStop) {
              return
            }
          }

          readStream()
        }).catch(error => {
          console.error('Stream read error:', error)
          onError && onError(error)
        })
      }

      readStream()
    })
    .catch(error => {
      console.error('Fetch error:', error)
      onError && onError(error)
    })
}

export const generateContent = (outline, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/generate-content',
    { outline, message: outline },
    onMessage,
    onError,
    onComplete
  )
}

export const continueWriting = (content, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/continue-writing',
    { content, message: content },
    onMessage,
    onError,
    onComplete
  )
}

export const optimizeContent = (content, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/optimize',
    { content, message: content },
    onMessage,
    onError,
    onComplete
  )
}

export const articleQA = (articleContent, question, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/article-qa',
    { articleContent, message: question },
    onMessage,
    onError,
    onComplete
  )
}

export const extractSummary = (content, onMessage, onError, onComplete) => {
  createSSEConnection(
    '/ai/extract-summary',
    { content, message: content },
    onMessage,
    onError,
    onComplete
  )
}
