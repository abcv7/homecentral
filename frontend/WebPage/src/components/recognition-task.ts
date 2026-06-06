export type TaskStatus =
  | 'progress'
  | 'result'
  | 'importing'
  | 'imported'
  | 'error'
  | 'cancelled'
  | 'autoImporting'

export interface RecognizedParcel {
  index: number
  courierCompany: string | null
  trackingNumber: string | null
  pickupCode: string | null
  ownerName: string | null
  arrivedDate: string | null
  productName: string | null
  confidence: string
  selected: boolean
}

export interface FailureItem {
  index: number
  item: string
  reason: string
}

export interface ImportResult {
  successCount: number
  failureCount: number
  failureItems: FailureItem[]
}

export interface PendingServerTask {
  id: number
  status: 'processing' | 'completed' | 'failed' | 'imported' | 'cancelled'
  resultJson: string | null
  failureMessage: string | null
  createdAt: string
  completedAt: string | null
  expiresAt: string | null
}

export interface RecognitionTask {
  id: string
  pendingId?: number
  title: string
  mode: 'image' | 'sms'
  status: TaskStatus
  createdAt: number
  controller: AbortController
  file?: File
  smsText?: string
  progressPercent: number
  progressMessage: string
  progressStage: string
  parcels: RecognizedParcel[]
  errorMessage: string
  importResult?: ImportResult
  _notified?: boolean
  countdown?: number
  countdownTimerId?: number
}

let imageCounter = 0
let smsCounter = 0

export function nextImageTitle(): string {
  imageCounter += 1
  return `图片${imageCounter}`
}

export function nextSmsTitle(): string {
  smsCounter += 1
  return `短信${smsCounter}`
}

export function resetCounters() {
  imageCounter = 0
  smsCounter = 0
}

export function createImageTask(id: string, file: File): RecognitionTask {
  return {
    id,
    title: nextImageTitle(),
    mode: 'image',
    status: 'progress',
    createdAt: Date.now(),
    controller: new AbortController(),
    file,
    progressPercent: 10,
    progressMessage: '正在读取图片...',
    progressStage: 'reading',
    parcels: [],
    errorMessage: '',
  }
}

export function createSmsTask(id: string, smsText: string): RecognitionTask {
  return {
    id,
    title: nextSmsTitle(),
    mode: 'sms',
    status: 'progress',
    createdAt: Date.now(),
    controller: new AbortController(),
    smsText,
    progressPercent: 20,
    progressMessage: '正在分析短信内容...',
    progressStage: 'analyzing',
    parcels: [],
    errorMessage: '',
  }
}

export function createPendingProcessingTask(pending: PendingServerTask): RecognitionTask {
  return {
    id: `pending-${pending.id}`,
    pendingId: pending.id,
    title: `服务器处理中 #${pending.id}`,
    mode: 'image',
    status: 'progress',
    createdAt: new Date(pending.createdAt).getTime(),
    controller: new AbortController(),
    progressPercent: 50,
    progressMessage: '服务器正在识别（页面关闭后仍在后台处理）...',
    progressStage: 'analyzing',
    parcels: [],
    errorMessage: '',
  }
}

export function restoreFromPending(pending: PendingServerTask): RecognitionTask | null {
  if (pending.status === 'processing') {
    return createPendingProcessingTask(pending)
  }
  if (pending.status === 'completed' && pending.resultJson) {
    let parsed: any
    try {
      parsed = JSON.parse(pending.resultJson)
    } catch {
      return null
    }
    const recognized = (parsed?.parcels || []).map((p: any, idx: number) => ({
      index: idx,
      courierCompany: p.courierCompany,
      trackingNumber: p.trackingNumber,
      pickupCode: p.pickupCode,
      ownerName: p.ownerName,
      arrivedDate: p.arrivedDate,
      productName: p.productName,
      confidence: p.confidence || 'MEDIUM',
      selected: true,
    }))
    if (recognized.length === 0) {
      return {
        id: `pending-${pending.id}`,
        pendingId: pending.id,
        title: `已识别 #${pending.id}`,
        mode: 'image',
        status: 'error',
        createdAt: new Date(pending.createdAt).getTime(),
        controller: new AbortController(),
        progressPercent: 100,
        progressMessage: '识别完成',
        progressStage: 'done',
        parcels: [],
        errorMessage: '未能识别出任何快递信息',
      }
    }
    return {
      id: `pending-${pending.id}`,
      pendingId: pending.id,
      title: `已识别 #${pending.id}`,
      mode: 'image',
      status: 'result',
      createdAt: new Date(pending.createdAt).getTime(),
      controller: new AbortController(),
      progressPercent: 100,
      progressMessage: '识别完成',
      progressStage: 'done',
      parcels: recognized,
      errorMessage: '',
    }
  }
  if (pending.status === 'failed') {
    return {
      id: `pending-${pending.id}`,
      pendingId: pending.id,
      title: `识别失败 #${pending.id}`,
      mode: 'image',
      status: 'error',
      createdAt: new Date(pending.createdAt).getTime(),
      controller: new AbortController(),
      progressPercent: 100,
      progressMessage: '识别失败',
      progressStage: 'done',
      parcels: [],
      errorMessage: pending.failureMessage || 'AI 识别失败',
    }
  }
  return null
}

export function getAccessToken(): string {
  return localStorage.getItem('access_token') || ''
}

function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result as string
      const base64 = result.split(',')[1]
      resolve(base64)
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

export async function runTask(
  task: RecognitionTask,
  tokenAccessor: () => string,
): Promise<void> {
  try {
    let body: Record<string, string>
    if (task.mode === 'image') {
      if (!task.file) throw new Error('图片文件丢失')
      task.progressPercent = 30
      task.progressMessage = '正在读取图片...'
      const base64 = await fileToBase64(task.file)
      if (task.controller.signal.aborted) return
      task.progressPercent = 50
      task.progressMessage = '正在调用 AI 识别...'
      body = { imageBase64: base64 }
    } else {
      task.progressPercent = 40
      task.progressMessage = '正在分析短信内容...'
      body = { smsText: task.smsText || '' }
    }

    const res = await fetch('/api/parcel/recognize', {
      method: 'POST',
      body: JSON.stringify(body),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${tokenAccessor()}`,
      },
      signal: task.controller.signal,
    })

    if (task.controller.signal.aborted) return

    task.progressPercent = 80
    task.progressMessage = '正在解析结果...'

    const data = await res.json()
    handleRecognitionResponse(task, data)
  } catch (err) {
    if ((err as any)?.name === 'AbortError') return
    task.status = 'error'
    task.errorMessage = '识别失败: ' + (err instanceof Error ? err.message : '未知错误')
  }
}

function handleRecognitionResponse(task: RecognitionTask, data: any) {
  if (data?.success && data.data) {
    if (data.data.pendingId) {
      task.pendingId = data.data.pendingId
    }
    const recognized = (data.data.parcels || []).map((p: any, idx: number) => ({
      index: idx,
      courierCompany: p.courierCompany,
      trackingNumber: p.trackingNumber,
      pickupCode: p.pickupCode,
      ownerName: p.ownerName,
      arrivedDate: p.arrivedDate,
      productName: p.productName,
      confidence: p.confidence || 'MEDIUM',
      selected: true,
    }))

    if (recognized.length === 0) {
      task.status = 'error'
      task.errorMessage = '未能识别出任何快递信息'
      return
    }

    task.parcels = recognized
    task.status = 'result'
    task.progressPercent = 100
    task.progressMessage = '识别完成'
  } else {
    task.status = 'error'
    task.errorMessage = data?.message || '识别失败'
  }
}
