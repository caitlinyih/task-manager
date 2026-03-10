import { useState } from 'react';
import { createTask } from '../api';

export default function TaskForm({ onCancel, onCreated }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState('TODO');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState(null);

  const validate = () => {
    const e = {};
    if (!title.trim()) e.title = 'Enter a title';
    if (!date) e.date = 'Enter a due date';
    if (!time) e.time = 'Enter a due time';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const fieldErrors = validate();
    if (Object.keys(fieldErrors).length) { setErrors(fieldErrors); return; }

    try {
      await createTask({ title, description, status, dueDateTime: `${date}T${time}:00` });
      onCreated();
    } catch (err) {
      setSubmitError(err.message);
    }
  };

  return (
    <>
      <h1>Create a task</h1>
      {submitError && <div className="error-banner">{submitError}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title</label>
          {errors.title && <span className="field-error">{errors.title}</span>}
          <input id="title" value={title} onChange={(e) => setTitle(e.target.value)} maxLength={255} />
          <span className="char-count">{title.length}/255</span>
        </div>
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea id="description" value={description} onChange={(e) => setDescription(e.target.value)} rows={3} maxLength={1000} />
          <span className="char-count">{description.length}/1000</span>
        </div>
        <div className="form-group">
          <label htmlFor="status">Status</label>
          <select id="status" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="TODO">To do</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="date">Due date</label>
          {errors.date && <span className="field-error">{errors.date}</span>}
          <input id="date" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>
        <div className="form-group">
          <label htmlFor="time">Due time</label>
          {errors.time && <span className="field-error">{errors.time}</span>}
          <input id="time" type="time" value={time} onChange={(e) => setTime(e.target.value)} />
        </div>
        <button type="submit" className="btn btn-primary">Create task</button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      </form>
    </>
  );
}
