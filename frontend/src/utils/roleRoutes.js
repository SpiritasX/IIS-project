export function homePathForRole(role) {
  switch (role) {
    case 'ADMIN':
      return '/admin'
    case 'BOTANIST':
      return '/botanist'
    case 'WORKER':
      return '/worker'
    case 'CUSTOMER':
    default:
      return '/home'
  }
}
