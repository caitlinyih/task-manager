import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import App from './App';
import * as api from './api';

vi.mock('./api');

const mockTasks = [
  { id: 1, title: 'Test Task', description: 'A test description', status: 'TODO', dueDateTime: '2026-06-01T10:00:00' },
];

beforeEach(() => {
  api.fetchTasks.mockResolvedValue(mockTasks);
  api.fetchTask.mockResolvedValue(mockTasks[0]);
  api.createTask.mockResolvedValue({ id: 2 });
  api.updateTaskStatus.mockResolvedValue({ ...mockTasks[0], status: 'COMPLETED' });
  api.deleteTask.mockResolvedValue();
});

afterEach(() => vi.clearAllMocks());

test('renders task list on load', async () => {
  render(<App />);
  await waitFor(() => {
    expect(screen.getByText('Test Task')).toBeInTheDocument();
  });
  expect(screen.getByText('To do')).toBeInTheDocument();
});

test('shows create form when button clicked', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Create task'));
  expect(screen.getByText('Create a task')).toBeInTheDocument();
  expect(screen.getByLabelText('Title')).toBeInTheDocument();
});

test('shows task detail when task clicked', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Test Task'));
  fireEvent.click(screen.getByText('Test Task'));
  await waitFor(() => {
    expect(api.fetchTask).toHaveBeenCalledWith(1);
  });
});

test('shows validation errors on empty form submit', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Create task', { selector: 'button[type="submit"]' }));
  expect(screen.getByText('Enter a title')).toBeInTheDocument();
  expect(screen.getByText('Enter a due date')).toBeInTheDocument();
  expect(screen.getByText('Enter a due time')).toBeInTheDocument();
});

test('navigates back to list from create form', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Cancel'));
  await waitFor(() => {
    expect(screen.getByText('Tasks')).toBeInTheDocument();
  });
});

test('shows error when backend is unavailable', async () => {
  api.fetchTasks.mockRejectedValue(new Error('Connection refused'));
  render(<App />);
  await waitFor(() => {
    expect(screen.getByText(/Unable to load tasks/)).toBeInTheDocument();
  });
});

test('creates a task with valid form data', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Create task'));

  fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'New Task' } });
  fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'A description' } });
  fireEvent.change(screen.getByLabelText('Due date'), { target: { value: '2026-06-15' } });
  fireEvent.change(screen.getByLabelText('Due time'), { target: { value: '14:00' } });

  fireEvent.click(screen.getByText('Create task', { selector: 'button[type="submit"]' }));

  await waitFor(() => {
    expect(api.createTask).toHaveBeenCalledWith({
      title: 'New Task',
      description: 'A description',
      status: 'TODO',
      dueDateTime: '2026-06-15T14:00:00',
    });
  });
});

test('shows error banner when task creation fails', async () => {
  api.createTask.mockRejectedValue(new Error('Server error'));
  render(<App />);
  await waitFor(() => screen.getByText('Create task'));
  fireEvent.click(screen.getByText('Create task'));

  fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'Failing Task' } });
  fireEvent.change(screen.getByLabelText('Due date'), { target: { value: '2026-06-15' } });
  fireEvent.change(screen.getByLabelText('Due time'), { target: { value: '10:00' } });

  fireEvent.click(screen.getByText('Create task', { selector: 'button[type="submit"]' }));

  await waitFor(() => {
    expect(screen.getByText('Server error')).toBeInTheDocument();
  });
});

test('updates task status from detail view', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Test Task'));
  fireEvent.click(screen.getByText('Test Task'));

  await waitFor(() => screen.getByText('Update status'));

  fireEvent.change(screen.getByDisplayValue('To do'), { target: { value: 'COMPLETED' } });
  fireEvent.click(screen.getByText('Update status'));

  await waitFor(() => {
    expect(api.updateTaskStatus).toHaveBeenCalledWith(1, 'COMPLETED');
  });
});

test('deletes task after confirmation', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Test Task'));
  fireEvent.click(screen.getByText('Test Task'));

  await waitFor(() => screen.getByText('Delete task'));
  fireEvent.click(screen.getByText('Delete task'));

  expect(screen.getByText(/Are you sure/)).toBeInTheDocument();
  fireEvent.click(screen.getByText('Yes, delete'));

  await waitFor(() => {
    expect(api.deleteTask).toHaveBeenCalledWith(1);
  });
});

test('cancels delete and hides confirmation', async () => {
  render(<App />);
  await waitFor(() => screen.getByText('Test Task'));
  fireEvent.click(screen.getByText('Test Task'));

  await waitFor(() => screen.getByText('Delete task'));
  fireEvent.click(screen.getByText('Delete task'));

  expect(screen.getByText(/Are you sure/)).toBeInTheDocument();
  fireEvent.click(screen.getByText('Cancel'));

  expect(screen.queryByText(/Are you sure/)).not.toBeInTheDocument();
});

test('shows error when status update fails', async () => {
  api.updateTaskStatus.mockRejectedValue(new Error('Update failed'));
  render(<App />);
  await waitFor(() => screen.getByText('Test Task'));
  fireEvent.click(screen.getByText('Test Task'));

  await waitFor(() => screen.getByText('Update status'));
  fireEvent.click(screen.getByText('Update status'));

  await waitFor(() => {
    expect(screen.getByText('Failed to update status')).toBeInTheDocument();
  });
});
