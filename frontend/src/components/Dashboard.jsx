import { useState, useEffect } from 'react';
import './Dashboard.css';

function Dashboard({ user, onLogout }) {
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    // Vize için dummy data, Final'de API'den gelecek
    setTasks([
      {
        id: 1,
        title: 'Complete project documentation',
        description: 'Write comprehensive documentation for the task management system',
        category: 'Work',
        status: 'In Progress',
        dueDate: '2025-11-15',
        dueTime: '17:00',
      },
      {
        id: 2,
        title: 'Schedule team meeting',
        description: 'Organize weekly team sync meeting',
        category: 'Work',
        status: 'Pending',
        dueDate: '2025-11-12',
        dueTime: '10:00',
      },
      {
        id: 3,
        title: 'Buy groceries',
        description: 'Weekly grocery shopping',
        category: 'Personal',
        status: 'Completed',
        dueDate: '2025-11-07',
        dueTime: '18:00',
      },
    ]);
  }, []);

  const getStatusColor = (status) => {
    switch (status) {
      case 'Completed':
        return 'status-completed';
      case 'In Progress':
        return 'status-progress';
      case 'Pending':
        return 'status-pending';
      default:
        return '';
    }
  };

  const isDueSoon = (dueDate) => {
    const due = new Date(dueDate);
    const now = new Date();
    const diff = (due - now) / (1000 * 60 * 60 * 24); // days
    return diff <= 2 && diff >= 0;
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div>
          <h1>Task Dashboard</h1>
          <p>Welcome back, {user?.firstName} {user?.lastName}!</p>
        </div>
        <button onClick={onLogout} className="btn-logout">
          Logout
        </button>
      </header>

      <div className="stats-container">
        <div className="stat-card">
          <h3>Total Tasks</h3>
          <p className="stat-number">{tasks.length}</p>
        </div>
        <div className="stat-card">
          <h3>Completed</h3>
          <p className="stat-number completed">
            {tasks.filter(t => t.status === 'Completed').length}
          </p>
        </div>
        <div className="stat-card">
          <h3>In Progress</h3>
          <p className="stat-number progress">
            {tasks.filter(t => t.status === 'In Progress').length}
          </p>
        </div>
        <div className="stat-card">
          <h3>Pending</h3>
          <p className="stat-number pending">
            {tasks.filter(t => t.status === 'Pending').length}
          </p>
        </div>
      </div>

      <div className="tasks-section">
        <div className="section-header">
          <h2>Your Tasks</h2>
          <button className="btn-add">+ Add Task</button>
        </div>

        <div className="tasks-grid">
          {tasks.map((task) => (
            <div key={task.id} className={`task-card ${isDueSoon(task.dueDate) ? 'due-soon' : ''}`}>
              <div className="task-header">
                <h3>{task.title}</h3>
                <span className={`status-badge ${getStatusColor(task.status)}`}>
                  {task.status}
                </span>
              </div>
              <p className="task-description">{task.description}</p>
              <div className="task-meta">
                <span className="category-badge">{task.category}</span>
                <span className="due-date">
                  📅 {task.dueDate} at {task.dueTime}
                </span>
              </div>
              {isDueSoon(task.dueDate) && (
                <div className="alert-due">⚠️ Due soon!</div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
