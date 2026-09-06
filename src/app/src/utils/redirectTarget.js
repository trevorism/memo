// @trevorism/ui-auth sends an expired session to /login?next=<path>; the OAuth
// buttons and older links still use return_url.
function relativePathOrEmpty(candidate) {
  if (typeof candidate !== 'string' || !candidate.startsWith('/')) {
    return ''
  }
  return candidate.startsWith('//') || candidate.startsWith('/\\') ? '' : candidate
}

export function destinationFromQuery(query = {}) {
  return relativePathOrEmpty(query.next) || relativePathOrEmpty(query.return_url)
}
