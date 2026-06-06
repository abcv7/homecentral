/**
 * 内存版 SWR（stale-while-revalidate）
 *
 * 设计原则：数据都在后端；前端不持久化任何业务数据。
 * 此缓存仅在 JS 运行时内存中存在，页面刷新即清空。
 *
 * 用法：
 *   const data = await swr('parcels:list', () => listParcels(...), 5000)
 *
 * - TTL 内：直接返回缓存值（不发请求）
 * - TTL 外：先返回旧值（如果有），同时后台重发请求更新缓存
 * - 第一次：直接返回请求结果并写入缓存
 */

interface CacheEntry<T> {
  data: T
  ts: number
  inflight: Promise<T> | null
}

const store = new Map<string, CacheEntry<unknown>>()

function now(): number {
  return Date.now()
}

function isFresh<T>(entry: CacheEntry<T> | undefined, ttl: number): boolean {
  if (!entry) return false
  return now() - entry.ts < ttl
}

export interface SwrOptions {
  /** TTL 毫秒，默认 5000 */
  ttl?: number
  /** 强制 revalidate（即使在 TTL 内也重新请求） */
  force?: boolean
}

export async function swr<T>(
  key: string,
  fetcher: () => Promise<T>,
  options: SwrOptions | number = {},
): Promise<T> {
  const opts: SwrOptions = typeof options === 'number' ? { ttl: options } : options
  const ttl = opts.ttl ?? 5000
  const existing = store.get(key) as CacheEntry<T> | undefined

  // 在 TTL 内 + 未强制刷新 → 直接返回
  if (!opts.force && isFresh(existing, ttl)) {
    return existing!.data
  }

  // 有 in-flight 请求，复用它（避免重复请求）
  if (existing?.inflight) {
    return existing.inflight
  }

  // TTL 外：触发后台刷新
  const promise = fetcher()
    .then((data) => {
      store.set(key, { data, ts: now(), inflight: null })
      return data
    })
    .catch((err) => {
      // 失败时清理 inflight，保留旧值（如果有）
      if (existing) {
        store.set(key, { ...existing, inflight: null })
      } else {
        store.delete(key)
      }
      throw err
    })

  store.set(key, {
    data: existing?.data as T,
    ts: existing?.ts ?? 0,
    inflight: promise,
  })

  return promise
}

/** 手动失效某个 key（数据写后调用） */
export function invalidate(key: string): void {
  store.delete(key)
}

/** 手动失效匹配前缀的所有 key */
export function invalidatePrefix(prefix: string): void {
  for (const k of store.keys()) {
    if (k.startsWith(prefix)) store.delete(k)
  }
}

/** 清空全部（用于测试/登出） */
export function clearAll(): void {
  store.clear()
}

/** 当前缓存大小（用于调试） */
export function size(): number {
  return store.size
}
