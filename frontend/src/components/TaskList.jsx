import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';
import { STATUS_LABELS, formatDate } from '../utils';

export default function TaskList({ onCreateClick, onTaskClick }) {
  const [tasks, setTasks] = useState([]);
  const [error, setError] = useState(null);

  const load = () => fetchTasks().then(setTasks).catch(() => setError('Unable to load tasks'));

  useEffect(() => { load(); }, []);

  if (error) return <div className="error-banner">{error}</div>;

  return (
    <>
      <h1>Tasks</h1>
      <button className="btn btn-primary" onClick={onCreateClick}>Create task</button>
      {tasks.length === 0 ? (
        <p>No tasks yet.</p>
      ) : (
        <table className="task-table">
          <thead>
            <tr><th>Title</th><th>Description</th><th>Status</th><th>Due</th></tr>
          </thead>
          <tbody>
            {tasks.map((t) => (
              <tr key={t.id} onClick={() => onTaskClick(t.id)} style={{ cursor: 'pointer' }}>
                <td>{t.title}</td>
                <td>{t.description || '—'}</td>
                <td><span className={`tag tag-${t.status.toLowerCase()}`}>{STATUS_LABELS[t.status]}</span></td>
                <td>{formatDate(t.dueDateTime)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
