import { useState, useEffect } from 'react';
import { fetchTask, updateTaskStatus, deleteTask } from '../api';
import { STATUS_LABELS, formatDate } from '../utils';

export default function TaskDetail({ id, onBack }) {
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('');
  const [error, setError] = useState(null);
  const [confirmingDelete, setConfirmingDelete] = useState(false);

  useEffect(() => {
    fetchTask(id)
      .then((t) => { setTask(t); if (t) setStatus(t.status); })
      .catch(() => setError('Failed to load task'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleStatusUpdate = async () => {
    try {
      const updated = await updateTaskStatus(id, status);
      setTask(updated);
    } catch {
      setError('Failed to update status');
    }
  };

  const handleDelete = async () => {
    try {
      await deleteTask(id);
      onBack();
    } catch {
      setError('Failed to delete task');
    }
  };

  if (loading) return <p>Loading...</p>;
  if (!task) return <><p>Task not found.</p><button className="btn btn-secondary" onClick={onBack}>Back to tasks</button></>;

  return (
    <>
      {error && <div className="error-banner">{error}</div>}
      <h1>{task.title}</h1>
      <dl className="summary-list">
        <dt>Status</dt>
        <dd><span className={`tag tag-${task.status.toLowerCase()}`}>{STATUS_LABELS[task.status]}</span></dd>
        {task.description && <><dt>Description</dt><dd>{task.description}</dd></>}
        <dt>Due</dt>
        <dd>{formatDate(task.dueDateTime)}</dd>
      </dl>
      <div className="status-update">
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="TODO">To do</option>
          <option value="IN_PROGRESS">In progress</option>
          <option value="COMPLETED">Completed</option>
        </select>
        <button className="btn btn-primary btn-small" onClick={handleStatusUpdate}>Update status</button>
      </div>
      <div className="actions">
        <button className="btn btn-secondary" onClick={onBack}>Back to tasks</button>
        {confirmingDelete ? (
          <div className="delete-confirm">
            <p>Are you sure you want to delete this task? This action cannot be undone.</p>
            <button className="btn btn-danger" onClick={handleDelete}>Yes, delete</button>
            <button className="btn btn-secondary" onClick={() => setConfirmingDelete(false)}>Cancel</button>
          </div>
        ) : (
          <button className="btn btn-danger" onClick={() => setConfirmingDelete(true)}>Delete task</button>
        )}
      </div>
    </>
  );
}
