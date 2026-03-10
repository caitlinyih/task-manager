const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:4000';

export async function fetchTasks() {
  const res = await fetch(`${API_URL}/tasks`);
  if (!res.ok) throw new Error('Failed to fetch tasks');
  return res.json();
}

export async function fetchTask(id) {
  const res = await fetch(`${API_URL}/tasks/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Failed to fetch task');
  return res.json();
}

export async function createTask(task) {
  const res = await fetch(`${API_URL}/tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.fieldErrors ? Object.values(body.fieldErrors).join('. ') : 'Failed to create task');
  }
  return res.json();
}

export async function updateTaskStatus(id, status) {
  const res = await fetch(`${API_URL}/tasks/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (!res.ok) throw new Error('Failed to update status');
  return res.json();
}

export async function deleteTask(id) {
  const res = await fetch(`${API_URL}/tasks/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to delete task');
}
