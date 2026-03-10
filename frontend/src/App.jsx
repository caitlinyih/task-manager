import { useState } from 'react';
import TaskList from './components/TaskList';
import TaskForm from './components/TaskForm';
import TaskDetail from './components/TaskDetail';

export default function App() {
  const [view, setView] = useState({ page: 'list' });

  return (
    <>
      <header className="app-header">
        <div className="container">
          <a href="/" onClick={(e) => { e.preventDefault(); setView({ page: 'list' }); }}>
            Task Manager
          </a>
        </div>
      </header>
      <main className="container">
        <div className="main-content">
          {view.page === 'list' && (
            <TaskList
              onCreateClick={() => setView({ page: 'create' })}
              onTaskClick={(id) => setView({ page: 'detail', id })}
            />
          )}
          {view.page === 'create' && (
            <TaskForm onCancel={() => setView({ page: 'list' })} onCreated={() => setView({ page: 'list' })} />
          )}
          {view.page === 'detail' && (
            <TaskDetail id={view.id} onBack={() => setView({ page: 'list' })} />
          )}
        </div>
      </main>
    </>
  );
}
