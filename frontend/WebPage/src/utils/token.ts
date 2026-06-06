const ACCESS_TOKEN = 'access_token'
const REFRESH_TOKEN = 'refresh_token'
const USERNAME = 'username'

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

export function getUsername(): string | null {
  return localStorage.getItem(USERNAME)
}

export function setUsername(username: string): void {
  localStorage.setItem(USERNAME, username)
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN)
  localStorage.removeItem(REFRESH_TOKEN)
  localStorage.removeItem(USERNAME)
}
