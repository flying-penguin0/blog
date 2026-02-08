import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

class ChatWebSocket {
  constructor() {
    this.stompClient = null
    this.connected = false
    this.userId = null
    this.onStatusChanged = null
  }

  connect(onMessageReceived, onStatusChanged, userId) {
    this.userId = userId
    this.onStatusChanged = onStatusChanged
    
    const socket = new SockJS('http://localhost:8080/api/ws/chatroom')
    this.stompClient = Stomp.over(socket)

    // 禁用调试日志
    this.stompClient.debug = () => {}

    // 连接时发送userId
    const connectHeaders = {
      userId: userId.toString()
    }

    this.stompClient.connect(
      connectHeaders,
      () => {
        this.connected = true
        console.log('WebSocket connected')

        // 订阅聊天室消息
        this.stompClient.subscribe('/topic/chatroom', (message) => {
          const data = JSON.parse(message.body)
          onMessageReceived(data)
        })

        // 订阅用户状态变化
        this.stompClient.subscribe('/topic/chatroom/status', (message) => {
          onStatusChanged(message.body)
        })
      },
      (error) => {
        console.error('WebSocket connection error:', error)
        this.connected = false
        // 连接失败时通知用户下线
        this.notifyOffline()
      }
    )
    
    // 监听WebSocket断开
    socket.onclose = () => {
      console.log('WebSocket disconnected')
      this.connected = false
      this.notifyOffline()
    }
  }

  sendMessage(message, userId) {
    if (this.connected && this.stompClient) {
      const payload = {
        ...message,
        userId: userId
      }
      this.stompClient.send('/app/chatroom/send', {}, JSON.stringify(payload))
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect()
      this.connected = false
    }
  }
  
  notifyOffline() {
    if (this.userId && this.onStatusChanged) {
      // 通知状态变化回调
      this.onStatusChanged('offline:' + this.userId)
    }
  }
}

export default new ChatWebSocket()
