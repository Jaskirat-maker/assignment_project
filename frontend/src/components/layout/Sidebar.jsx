import { NavLink } from 'react-router-dom'

const linkClassName = ({ isActive }) =>
  [
    'block rounded-lg px-3 py-2 text-sm font-medium transition-colors',
    isActive
      ? 'bg-indigo-100 text-indigo-700'
      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
  ].join(' ')

export default function Sidebar({ user, onLogout }) {
  return (
    <aside className="flex w-72 flex-col border-r border-slate-200 bg-white p-6">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Finance App</p>
        <h1 className="mt-2 text-2xl font-bold text-slate-900">Dashboard</h1>
      </div>

      <nav className="mt-8 space-y-2">
        <NavLink to="/dashboard" className={linkClassName}>
          Overview
        </NavLink>
      </nav>

      <div className="mt-auto rounded-xl border border-slate-200 bg-slate-50 p-4">
        <p className="text-sm font-semibold text-slate-800">{user?.username}</p>
        <p className="text-xs text-slate-500">{user?.email}</p>
        <p className="mt-1 inline-flex rounded-full bg-indigo-50 px-2 py-1 text-xs font-medium text-indigo-700">
          {user?.role}
        </p>
        <button
          type="button"
          onClick={onLogout}
          className="mt-4 w-full rounded-lg bg-slate-900 px-3 py-2 text-sm font-semibold text-white transition hover:bg-slate-700"
        >
          Sign out
        </button>
      </div>
    </aside>
  )
}
