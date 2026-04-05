export function getApiErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (error?.response?.data) {
    const { data } = error.response

    if (typeof data === 'string') {
      return data
    }

    if (data.message) {
      return data.message
    }

    if (data.error) {
      return data.error
    }
  }

  return fallback
}
