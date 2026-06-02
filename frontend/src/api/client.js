import axios from "axios"

const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
})

const unsafeMethods = new Set(['post', 'put', 'patch', 'delete'])
const authMutationPaths = new Set(['/api/auth/login', '/api/auth/logout', '/api/auth/signup'])
let csrfTokenRequest = null

function readCookie(name) {
  const cookie = document.cookie
    .split('; ')
    .find((item) => item.startsWith(`${name}=`))

  return cookie ? decodeURIComponent(cookie.substring(name.length + 1)) : ''
}

function clearCookie(name) {
  document.cookie = `${name}=; Max-Age=0; path=/`
}

function requestPath(config) {
  const url = config.url || ''

  if (url.startsWith('http')) {
    return new URL(url).pathname
  }

  return url.split('?')[0]
}

async function fetchCsrfToken() {
  if (!csrfTokenRequest) {
    csrfTokenRequest = api
      .get('/api/auth/csrf', { skipCsrf: true })
      .then((response) => readCookie('XSRF-TOKEN') || response.data.token)
      .finally(() => {
        csrfTokenRequest = null
      })
  }

  return csrfTokenRequest
}

async function ensureCsrfToken({ forceRefresh = false } = {}) {
  const existingToken = readCookie('XSRF-TOKEN')

  if (existingToken && !forceRefresh) {
    return existingToken
  }

  return fetchCsrfToken()
}

api.interceptors.request.use(async (config) => {
  const method = (config.method || 'get').toLowerCase()

  if (unsafeMethods.has(method) && !config.skipCsrf) {
    const token = await ensureCsrfToken({
      forceRefresh: authMutationPaths.has(requestPath(config)),
    })

    if (token) {
      config.headers = config.headers || {}
      config.headers['X-XSRF-TOKEN'] = token
    }
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config || {}
    const method = (originalRequest.method || 'get').toLowerCase()

    if (
      error.response?.status === 403 &&
      unsafeMethods.has(method) &&
      !originalRequest.skipCsrf &&
      !originalRequest.csrfRetry
    ) {
      originalRequest.csrfRetry = true
      clearCookie('XSRF-TOKEN')

      const token = await ensureCsrfToken({ forceRefresh: true })
      originalRequest.headers = originalRequest.headers || {}
      originalRequest.headers['X-XSRF-TOKEN'] = token

      return api(originalRequest)
    }

    if (error.response?.status === 401 && typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('iis:unauthorized'))
    }

    return Promise.reject(error)
  },
)

export default api
