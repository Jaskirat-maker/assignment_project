export const runtimeConfig = {
  useMockApi: import.meta.env.VITE_USE_MOCK === 'true',
}

export const useMockApi = runtimeConfig.useMockApi
