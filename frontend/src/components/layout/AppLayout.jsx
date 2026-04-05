import Sidebar from './Sidebar'

export default function AppLayout({ user, onLogout, children }) {
  return (
    <div className="flex min-h-screen">
      <Sidebar user={user} onLogout={onLogout} />
      <main className="flex-1 p-6 md:p-10">{children}</main>
    </div>
  )
}
