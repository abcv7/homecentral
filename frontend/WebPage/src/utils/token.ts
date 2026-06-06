const ACCESS_TOKEN = 'access_token'
const REFRESH_TOKEN = 'refresh_token'

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN)
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN, token)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN)
}

export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN, token)
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN)
  localStorage.removeItem(REFRESH_TOKEN)
}
