import { useState, useEffect } from 'react';
import { taskService } from '../services/api';
import api from '../services/api';
import FileManager from './FileManager';
import Statistics from './Statistics';
import './AdminDashboard.css';

function AdminDashboard({ user, onLogout }) {
  const [activeTab, setActiveTab] = useState('tasks');
  const [allTasks, setAllTasks] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Edit Modal State
  const [showEditModal, setShowEditModal] = useState(false);
  const [editTask, setEditTask] = useState(null);
  const [editLoading, setEditLoading] = useState(false);

  // Add Task Modal State
  const [showAddModal, setShowAddModal] = useState(false);
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    category: 'Work',
    status: 'Pending',
    dueDate: '',
    dueTime: '',
    assignedUserId: '',
  });
  const [addLoading, setAddLoading] = useState(false);

  // File Manager State
  const [showFileManager, setShowFileManager] = useState(null);

  // Statistics State
  const [showStats, setShowStats] = useState(false);

  // Filter States
  const [filterCategory, setFilterCategory] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError('');

    try {
      const tasksResult = await taskService.getAllTasks();
      if (tasksResult.success) {
        setAllTasks(tasksResult.data || []);
      }

      try {
        const usersResponse = await api.get('/api/auth/get');
        setAllUsers(usersResponse.data || []);
      } catch (err) {
        console.log('Could not fetch users:', err);
      }
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  // Filtered tasks
  const filteredTasks = allTasks.filter(task => {
    const categoryMatch = filterCategory === 'all' || task.category === filterCategory;
    const statusMatch = filterStatus === 'all' || task.status === filterStatus;
    return categoryMatch && statusMatch;
  });

  // Stats
  const stats = {
    totalTasks: allTasks.length,
    completedTasks: allTasks.filter(t => t.status === 'Completed').length,
    pendingTasks: allTasks.filter(t => t.status === 'Pending').length,
    inProgressTasks: allTasks.filter(t => t.status === 'In Progress').length,
    totalUsers: allUsers.length,
  };

  // Delete handler
  const handleDeleteTask = async (taskId) => {
    const confirmed = window.confirm('Are you sure you want to delete this task?');
    if (!confirmed) return;

    const result = await taskService.deleteTask(taskId);
    if (result.success) {
      setAllTasks(prev => prev.filter(t => t.id !== taskId));
    } else {
      alert('Failed to delete task');
    }
  };

  // Edit handlers
  const openEditModal = (task) => {
    setEditTask({ ...task });
    setShowEditModal(true);
  };

  const handleEditChange = (e) => {
    setEditTask(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleUpdateTask = async (e) => {
    e.preventDefault();
    if (!editTask) return;

    const { id, title, description, category, status, dueDate, dueTime, assignedUserId } = editTask;
    const payload = { title, description, category, status, dueDate, dueTime };

    if (assignedUserId) {
      payload.assignedUserId = Number(assignedUserId);
    }

    setEditLoading(true);
    try {
      const result = await taskService.updateTask(id, payload);
      if (result.success) {
        setAllTasks(prev =>
          prev.map(t => t.id === id ? (result.data || { ...t, ...payload }) : t)
        );
        setShowEditModal(false);
        setEditTask(null);
      } else {
        alert('Failed to update task: ' + result.error);
      }
    } catch (err) {
      alert('Failed to update task');
    } finally {
      setEditLoading(false);
    }
  };

  // Add Task handlers
  const handleAddChange = (e) => {
    setNewTask(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleAddTask = async (e) => {
    e.preventDefault();

    const taskData = { ...newTask };
    if (taskData.assignedUserId) {
      taskData.assignedUserId = Number(taskData.assignedUserId);
    } else {
      delete taskData.assignedUserId;
    }

    setAddLoading(true);
    try {
      const result = await taskService.createTask(taskData);
      if (result.success) {
        setAllTasks(prev => [...prev, result.data]);
        setShowAddModal(false);
        setNewTask({
          title: '',
          description: '',
          category: 'Work',
          status: 'Pending',
          dueDate: '',
          dueTime: '',
          assignedUserId: '',
        });
      } else {
        alert('Failed to create task: ' + result.error);
      }
    } catch (err) {
      alert('Failed to create task');
    } finally {
      setAddLoading(false);
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'Completed': return 'badge-completed';
      case 'In Progress': return 'badge-progress';
      case 'Pending': return 'badge-pending';
      default: return '';
    }
  };

  const getUserName = (userId) => {
    const foundUser = allUsers.find(u => u.id === userId);
    return foundUser ? `${foundUser.firstName} ${foundUser.lastName}` : '-';
  };

  return (
    <div className="admin-dashboard">
      {/* Header */}
      <header className="admin-header">
        <div className="header-left">
          <h1>Admin Panel</h1>
          <p>Welcome, {user.firstName} {user.lastName}</p>
        </div>
        <div className="header-actions">
          <button className="btn-stats" onClick={() => setShowStats(true)}>
            Statistics
          </button>
          <button className="btn-logout" onClick={onLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Stats Cards */}
      <div className="stats-container">
        <div className="stat-card">
          <h3>Total Users</h3>
          <p className="stat-number">{stats.totalUsers}</p>
        </div>
        <div className="stat-card">
          <h3>Total Tasks</h3>
          <p className="stat-number">{stats.totalTasks}</p>
        </div>
        <div className="stat-card">
          <h3>Completed</h3>
          <p className="stat-number completed">{stats.completedTasks}</p>
        </div>
        <div className="stat-card">
          <h3>In Progress</h3>
          <p className="stat-number progress">{stats.inProgressTasks}</p>
        </div>
        <div className="stat-card">
          <h3>Pending</h3>
          <p className="stat-number pending">{stats.pendingTasks}</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs-container">
        <div className="tabs">
          <button
            className={`tab-btn ${activeTab === 'tasks' ? 'active' : ''}`}
            onClick={() => setActiveTab('tasks')}
          >
            All Tasks ({allTasks.length})
          </button>
          <button
            className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            All Users ({allUsers.length})
          </button>
        </div>

        {activeTab === 'tasks' && (
          <button className="btn-add" onClick={() => setShowAddModal(true)}>
            + Add Task
          </button>
        )}
      </div>

      {/* Filter Section - Only for tasks */}
      {activeTab === 'tasks' && (
        <div className="filter-section">
          <div className="filter-group">
            <label>Category:</label>
            <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}>
              <option value="all">All Categories</option>
              <option value="Work">Work</option>
              <option value="Personal">Personal</option>
              <option value="Shopping">Shopping</option>
              <option value="Health">Health</option>
              <option value="Other">Other</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Status:</label>
            <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
              <option value="all">All Statuses</option>
              <option value="Pending">Pending</option>
              <option value="In Progress">In Progress</option>
              <option value="Completed">Completed</option>
            </select>
          </div>
          {(filterCategory !== 'all' || filterStatus !== 'all') && (
            <button className="btn-clear-filter" onClick={() => { setFilterCategory('all'); setFilterStatus('all'); }}>
              Clear Filters
            </button>
          )}
          <span className="filter-result">
            Showing {filteredTasks.length} of {allTasks.length} tasks
          </span>
        </div>
      )}

      {/* Content */}
      <div className="content-section">
        {loading ? (
          <div className="loading-state">Loading...</div>
        ) : error ? (
          <div className="error-state">{error}</div>
        ) : activeTab === 'tasks' ? (
          <div className="table-container">
            {filteredTasks.length === 0 ? (
              <p className="empty-message">No tasks found.</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Owner</th>
                    <th>Category</th>
                    <th>Status</th>
                    <th>Due Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTasks.map(task => (
                    <tr key={task.id}>
                      <td>#{task.id}</td>
                      <td className="title-cell">{task.title}</td>
                      <td className="owner-cell">{getUserName(task.userId)}</td>
                      <td><span className="category-tag">{task.category}</span></td>
                      <td>
                        <span className={`status-badge ${getStatusBadgeClass(task.status)}`}>
                          {task.status}
                        </span>
                      </td>
                      <td>{task.dueDate} {task.dueTime && `${task.dueTime}`}</td>
                      <td className="actions-cell">
                        <button
                          className="btn-action btn-files"
                          onClick={() => setShowFileManager({ taskId: task.id, taskTitle: task.title })}
                        >
                          Files
                        </button>
                        <button
                          className="btn-action btn-edit"
                          onClick={() => openEditModal(task)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn-action btn-delete"
                          onClick={() => handleDeleteTask(task.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        ) : (
          <div className="table-container">
            {allUsers.length === 0 ? (
              <p className="empty-message">No users found.</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Role</th>
                  </tr>
                </thead>
                <tbody>
                  {allUsers.map((u) => (
                    <tr key={u.id}>
                      <td>#{u.id}</td>
                      <td>{u.firstName} {u.lastName}</td>
                      <td>{u.mail}</td>
                      <td>{u.phone || '-'}</td>
                      <td>
                        <span className={`role-badge ${u.role === 'ADMIN' ? 'role-admin' : 'role-user'}`}>
                          {u.role}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {/* Add Task Modal */}
      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add New Task</h3>
              <button className="close-btn" onClick={() => setShowAddModal(false)}>x</button>
            </div>

            <form onSubmit={handleAddTask}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  name="title"
                  value={newTask.title}
                  onChange={handleAddChange}
                  required
                  placeholder="Task title"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={newTask.description}
                  onChange={handleAddChange}
                  rows="3"
                  placeholder="Task description"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Category</label>
                  <select name="category" value={newTask.category} onChange={handleAddChange}>
                    <option value="Work">Work</option>
                    <option value="Personal">Personal</option>
                    <option value="Shopping">Shopping</option>
                    <option value="Health">Health</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Status</label>
                  <select name="status" value={newTask.status} onChange={handleAddChange}>
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
                    onChange={handleAddChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Due Time</label>
                  <input
                    type="time"
                    name="dueTime"
                    value={newTask.dueTime}
                    onChange={handleAddChange}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Assign to User *</label>
                <select
                  name="assignedUserId"
                  value={newTask.assignedUserId}
                  onChange={handleAddChange}
                  required
                >
                  <option value="">Select a user</option>
                  {allUsers.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.firstName} {u.lastName} ({u.mail})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-actions">
                <button type="button" className="btn-cancel" onClick={() => setShowAddModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-save" disabled={addLoading}>
                  {addLoading ? 'Creating...' : 'Create Task'}
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
              <h3>Edit Task</h3>
              <button className="close-btn" onClick={() => setShowEditModal(false)}>x</button>
            </div>

            <form onSubmit={handleUpdateTask}>
              <div className="form-group">
                <label>Title</label>
                <input
                  type="text"
                  name="title"
                  value={editTask.title || ''}
                  onChange={handleEditChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={editTask.description || ''}
                  onChange={handleEditChange}
                  rows="3"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Category</label>
                  <select name="category" value={editTask.category || ''} onChange={handleEditChange}>
                    <option value="Work">Work</option>
                    <option value="Personal">Personal</option>
                    <option value="Shopping">Shopping</option>
                    <option value="Health">Health</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Status</label>
                  <select name="status" value={editTask.status || 'Pending'} onChange={handleEditChange}>
                    <option value="Pending">Pending</option>
                    <option value="In Progress">In Progress</option>
                    <option value="Completed">Completed</option>
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Due Date</label>
                  <input
                    type="date"
                    name="dueDate"
                    value={editTask.dueDate || ''}
                    onChange={handleEditChange}
                  />
                </div>

                <div className="form-group">
                  <label>Due Time</label>
                  <input
                    type="time"
                    name="dueTime"
                    value={editTask.dueTime || ''}
                    onChange={handleEditChange}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Reassign to User</label>
                <select
                  name="assignedUserId"
                  value={editTask.assignedUserId || ''}
                  onChange={handleEditChange}
                >
                  <option value="">Keep current owner</option>
                  {allUsers.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.firstName} {u.lastName} ({u.mail})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-actions">
                <button type="button" className="btn-cancel" onClick={() => setShowEditModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-save" disabled={editLoading}>
                  {editLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* File Manager Modal */}
      {showFileManager && (
        <FileManager
          taskId={showFileManager.taskId}
          taskTitle={showFileManager.taskTitle}
          onClose={() => setShowFileManager(null)}
        />
      )}

      {/* Statistics Modal */}
      {showStats && (
        <Statistics
          tasks={allTasks}
          onClose={() => setShowStats(false)}
        />
      )}
    </div>
  );
}

export default AdminDashboard;
