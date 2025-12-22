import { useState, useEffect, useMemo } from 'react';
import { taskService } from '../services/api';
import Statistics from './Statistics';
import FileManager from './FileManager';
import './Dashboard.css';

function Dashboard({ user, onLogout }) {
  const [tasks, setTasks] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showStats, setShowStats] = useState(false);
  const [showFileManager, setShowFileManager] = useState(null); // { taskId, taskTitle }
  const [loading, setLoading] = useState(false);            // genel loading (add)
  const [rowLoadingId, setRowLoadingId] = useState(null);   // tek satır loading (edit/delete)

  // Filtreleme state'leri
  const [filterCategory, setFilterCategory] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');

  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    category: '',
    status: 'Pending',
    dueDate: '',
    dueTime: '',
  });
  const [editTask, setEditTask] = useState(null); // {id, title, ...}

  // Görevleri yükle
  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    setLoading(true);
    const result = await taskService.getAllTasks();
    setLoading(false);

    if (result.success) {
      setTasks(result.data || []);
    } else {
      console.error('Failed to fetch tasks:', result.error);
    }
  };

  // ------- ADD -------
  const handleInputChange = (e) => {
    setNewTask({ ...newTask, [e.target.name]: e.target.value });
  };

  const handleAddTask = async (e) => {
    e.preventDefault();
    setLoading(true);

    const result = await taskService.createTask(newTask);
    setLoading(false);

    if (result.success) {
      setTasks((prev) => [...prev, result.data]);
      setShowAddModal(false);
      setNewTask({
        title: '',
        description: '',
        category: '',
        status: 'Pending',
        dueDate: '',
        dueTime: '',
      });
    } else {
      alert('Failed to create task: ' + result.error);
    }
  };

  // ------- EDIT -------
  const openEdit = (task) => {
    // form kontrollü olsun diye kopya
    setEditTask({ ...task });
    setShowEditModal(true);
  };

  const handleEditChange = (e) => {
    setEditTask((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleUpdateTask = async (e) => {
    e.preventDefault();
    if (!editTask) return;

    const { id, title, description, category, status, dueDate, dueTime } = editTask;
    const payload = { title, description, category, status, dueDate, dueTime };

    try {
      setRowLoadingId(id);
      const result = await taskService.updateTask(id, payload);
      setTasks((prev) =>
        prev.map((t) =>
          t.id === id ? (result?.data ? result.data : { ...t, ...payload }) : t
        )
      );
      setShowEditModal(false);
      setEditTask(null);
    } catch (err) {
      alert('Failed to update task: ' + (err?.message || err));
    } finally {
      setRowLoadingId(null);
    }
  };

  // ------- DELETE -------
  const handleDeleteTask = async (id) => {
    const ok = window.confirm('Bu görevi silmek istiyor musun?');
    if (!ok) return;

    try {
      setRowLoadingId(id);
      const result = await taskService.deleteTask(id);
      if (result?.success || result === undefined) {
        setTasks((prev) => prev.filter((t) => t.id !== id));
      } else {
        alert('Failed to delete task: ' + result.error);
      }
    } catch (err) {
      alert('Failed to delete task: ' + (err?.message || err));
    } finally {
      setRowLoadingId(null);
    }
  };

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
    const diff = (due - now) / (1000 * 60 * 60 * 24);
    return diff <= 2 && diff >= 0;
  };

  const isOverdue = (dueDate) => {
    const due = new Date(dueDate);
    const now = new Date();
    return due < now;
  };

  // Filtrelenmiş görevler
  const filteredTasks = useMemo(() => {
    return tasks.filter(task => {
      const categoryMatch = filterCategory === 'all' || task.category === filterCategory;
      const statusMatch = filterStatus === 'all' || task.status === filterStatus;
      return categoryMatch && statusMatch;
    });
  }, [tasks, filterCategory, filterStatus]);

  // Benzersiz kategoriler
  const categories = useMemo(() => {
    const cats = [...new Set(tasks.map(t => t.category).filter(Boolean))];
    return cats;
  }, [tasks]);

  // Task kartı için renk sınıfı
  const getTaskCardClass = (task) => {
    let classes = 'task-card';
    
    // Durum bazlı renk
    if (task.status === 'Completed') {
      classes += ' task-completed';
    } else if (task.status === 'In Progress') {
      classes += ' task-in-progress';
    }
    
    // Deadline uyarıları (sadece tamamlanmamış görevler için)
    if (task.status !== 'Completed') {
      if (isOverdue(task.dueDate)) {
        classes += ' task-overdue';
      } else if (isDueSoon(task.dueDate)) {
        classes += ' due-soon';
      }
    }
    
    return classes;
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div>
          <h1>Task Dashboard</h1>
          <p>Welcome back, {user?.firstName} {user?.lastName}!</p>
        </div>
        <div className="header-actions">
          <button onClick={() => setShowStats(true)} className="btn-stats">
            Statistics
          </button>
          <button onClick={onLogout} className="btn-logout">
            Logout
          </button>
        </div>
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

      {/* Filtreleme Bölümü */}
      <div className="filter-section">
        <div className="filter-group">
          <label>Category:</label>
          <select 
            value={filterCategory} 
            onChange={(e) => setFilterCategory(e.target.value)}
          >
            <option value="all">All Categories</option>
            {categories.map(cat => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
        </div>
        <div className="filter-group">
          <label>Status:</label>
          <select 
            value={filterStatus} 
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <option value="all">All Statuses</option>
            <option value="Pending">Pending</option>
            <option value="In Progress">In Progress</option>
            <option value="Completed">Completed</option>
          </select>
        </div>
        {(filterCategory !== 'all' || filterStatus !== 'all') && (
          <button 
            className="btn-clear-filter"
            onClick={() => { setFilterCategory('all'); setFilterStatus('all'); }}
          >
            Clear Filters
          </button>
        )}
        <span className="filter-result">
          Showing {filteredTasks.length} of {tasks.length} tasks
        </span>
      </div>

      <div className="tasks-section">
        <div className="section-header">
          <h2>Your Tasks</h2>
          <button
            className="btn-add"
            onClick={() => setShowAddModal(true)}
          >
            + Add Task
          </button>
        </div>

        {loading && !showAddModal ? (
          <p>Loading tasks...</p>
        ) : filteredTasks.length === 0 ? (
          <p>{tasks.length === 0 ? 'No tasks yet. Click "Add Task" to create one!' : 'No tasks match the current filters.'}</p>
        ) : (
          <div className="tasks-grid">
            {filteredTasks.map((task) => (
              <div
                key={task.id}
                className={getTaskCardClass(task)}
              >
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

                {/* Deadline Uyarıları */}
                {task.status !== 'Completed' && isOverdue(task.dueDate) && (
                  <div className="alert-overdue">🚨 Overdue!</div>
                )}
                {task.status !== 'Completed' && isDueSoon(task.dueDate) && !isOverdue(task.dueDate) && (
                  <div className="alert-due">⚠️ Due soon!</div>
                )}

                <div className="task-actions">
                  <button
                    className="btn-files"
                    onClick={() => setShowFileManager({ taskId: task.id, taskTitle: task.title })}
                    title="Manage Files"
                  >
                    📎
                  </button>
                  <button
                    className="btn-edit"
                    onClick={() => openEdit(task)}
                    disabled={rowLoadingId === task.id}
                  >
                    {rowLoadingId === task.id && showEditModal ? 'Opening...' : 'Edit'}
                  </button>
                  <button
                    className="btn-delete"
                    onClick={() => handleDeleteTask(task.id)}
                    disabled={rowLoadingId === task.id}
                  >
                    {rowLoadingId === task.id ? 'Deleting...' : 'Delete'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Task Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Add New Task</h2>
              <button
                className="modal-close"
                onClick={() => setShowAddModal(false)}
              >
                ×
              </button>
            </div>

            <form onSubmit={handleAddTask}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  name="title"
                  value={newTask.title}
                  onChange={handleInputChange}
                  required
                  placeholder="Enter task title"
                />
              </div>

              <div className="form-group">
                <label>Description *</label>
                <textarea
                  name="description"
                  value={newTask.description}
                  onChange={handleInputChange}
                  required
                  rows="3"
                  placeholder="Enter task description"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Category *</label>
                  <select
                    name="category"
                    value={newTask.category}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select category</option>
                    <option value="Work">Work</option>
                    <option value="Personal">Personal</option>
                    <option value="Shopping">Shopping</option>
                    <option value="Health">Health</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Status *</label>
                  <select
                    name="status"
                    value={newTask.status}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="Pending">Pending</option>
                    <option value="In Progress">In Progress</option>
                    <option value="Completed">Completed</option>
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Due Date *</label>
                  <input
                    type="date"
                    name="dueDate"
                    value={newTask.dueDate}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Due Time *</label>
                  <input
                    type="time"
                    name="dueTime"
                    value={newTask.dueTime}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={() => setShowAddModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-submit"
                  disabled={loading}
                >
                  {loading ? 'Creating...' : 'Create Task'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Task Modal */}
      {showEditModal && editTask && (
        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Edit Task</h2>
              <button
                className="modal-close"
                onClick={() => setShowEditModal(false)}
              >
                ×
              </button>
            </div>

            <form onSubmit={handleUpdateTask}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  name="title"
                  value={editTask.title || ''}
                  onChange={handleEditChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Description *</label>
                <textarea
                  name="description"
                  value={editTask.description || ''}
                  onChange={handleEditChange}
                  required
                  rows="3"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Category *</label>
                  <select
                    name="category"
                    value={editTask.category || ''}
                    onChange={handleEditChange}
                    required
                  >
                    <option value="">Select category</option>
                    <option value="Work">Work</option>
                    <option value="Personal">Personal</option>
                    <option value="Shopping">Shopping</option>
                    <option value="Health">Health</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Status *</label>
                  <select
                    name="status"
                    value={editTask.status || 'Pending'}
                    onChange={handleEditChange}
                    required
                  >
                    <option value="Pending">Pending</option>
                    <option value="In Progress">In Progress</option>
                    <option value="Completed">Completed</option>
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Due Date *</label>
                  <input
                    type="date"
                    name="dueDate"
                    value={editTask.dueDate || ''}
                    onChange={handleEditChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Due Time *</label>
                  <input
                    type="time"
                    name="dueTime"
                    value={editTask.dueTime || ''}
                    onChange={handleEditChange}
                    required
                  />
                </div>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={() => setShowEditModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-submit"
                  disabled={rowLoadingId === editTask.id}
                >
                  {rowLoadingId === editTask.id ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Statistics Modal */}
      {showStats && (
        <Statistics 
          tasks={tasks} 
          onClose={() => setShowStats(false)} 
        />
      )}

      {/* File Manager Modal */}
      {showFileManager && (
        <FileManager
          taskId={showFileManager.taskId}
          taskTitle={showFileManager.taskTitle}
          onClose={() => setShowFileManager(null)}
        />
      )}
    </div>
  );
}

export default Dashboard;
