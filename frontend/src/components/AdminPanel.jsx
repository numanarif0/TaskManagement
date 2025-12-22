import { useState, useEffect } from 'react';
import { taskService } from '../services/api';
import api from '../services/api';
import FileManager from './FileManager';
import './AdminPanel.css';

function AdminPanel({ onClose }) {
  const [activeTab, setActiveTab] = useState('tasks');
  const [allTasks, setAllTasks] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Edit Modal State
  const [showEditModal, setShowEditModal] = useState(false);
  const [editTask, setEditTask] = useState(null);
  const [editLoading, setEditLoading] = useState(false);

  // File Manager State
  const [showFileManager, setShowFileManager] = useState(null);

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

  // Stats
  const stats = {
    totalTasks: allTasks.length,
    completedTasks: allTasks.filter(t => t.status === 'Completed').length,
    pendingTasks: allTasks.filter(t => t.status === 'Pending').length,
    inProgressTasks: allTasks.filter(t => t.status === 'In Progress').length,
    totalUsers: allUsers.length,
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'Completed': return 'badge-completed';
      case 'In Progress': return 'badge-progress';
      case 'Pending': return 'badge-pending';
      default: return '';
    }
  };

  // Find user name by task (we don't have userId in task response, so we show "—" for now)
  // This would need backend change to include owner info in task response

  return (
    <div className="admin-overlay" onClick={onClose}>
      <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
        <div className="admin-header">
          <div>
            <h2>Admin Panel</h2>
            <p>Manage all tasks and users</p>
          </div>
          <button className="close-btn" onClick={onClose}>x</button>
        </div>

        {error && <div className="admin-error">{error}</div>}

        {/* Stats Overview */}
        <div className="admin-stats">
          <div className="admin-stat-card">
            <span className="stat-value">{stats.totalUsers}</span>
            <span className="stat-label">Users</span>
          </div>
          <div className="admin-stat-card">
            <span className="stat-value">{stats.totalTasks}</span>
            <span className="stat-label">Total Tasks</span>
          </div>
          <div className="admin-stat-card completed">
            <span className="stat-value">{stats.completedTasks}</span>
            <span className="stat-label">Completed</span>
          </div>
          <div className="admin-stat-card pending">
            <span className="stat-value">{stats.pendingTasks}</span>
            <span className="stat-label">Pending</span>
          </div>
        </div>

        {/* Tabs */}
        <div className="admin-tabs">
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

        {/* Content */}
        <div className="admin-content">
          {loading ? (
            <div className="loading-state">Loading...</div>
          ) : activeTab === 'tasks' ? (
            <div className="tasks-table-container">
              {allTasks.length === 0 ? (
                <p className="empty-message">No tasks found.</p>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Title</th>
                      <th>Category</th>
                      <th>Status</th>
                      <th>Due Date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allTasks.map(task => (
                      <tr key={task.id}>
                        <td>#{task.id}</td>
                        <td className="task-title-cell">{task.title}</td>
                        <td><span className="category-tag">{task.category}</span></td>
                        <td>
                          <span className={`status-badge ${getStatusBadgeClass(task.status)}`}>
                            {task.status}
                          </span>
                        </td>
                        <td>{task.dueDate}</td>
                        <td className="actions-cell">
                          <button
                            className="btn-action btn-files-small"
                            onClick={() => setShowFileManager({ taskId: task.id, taskTitle: task.title })}
                            title="Manage Files"
                          >
                            Files
                          </button>
                          <button
                            className="btn-action btn-edit-small"
                            onClick={() => openEditModal(task)}
                            title="Edit Task"
                          >
                            Edit
                          </button>
                          <button
                            className="btn-action btn-delete-small"
                            onClick={() => handleDeleteTask(task.id)}
                            title="Delete Task"
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
            <div className="users-table-container">
              {allUsers.length === 0 ? (
                <p className="empty-message">No users found.</p>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allUsers.map((user) => (
                      <tr key={user.id}>
                        <td>#{user.id}</td>
                        <td>{user.firstName} {user.lastName}</td>
                        <td>{user.mail}</td>
                        <td>
                          <span className={`role-badge ${user.role === 'ADMIN' ? 'role-admin' : 'role-user'}`}>
                            {user.role}
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
      </div>

      {/* Edit Task Modal */}
      {showEditModal && editTask && (
        <div className="edit-modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="edit-modal-header">
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
                  <select
                    name="category"
                    value={editTask.category || ''}
                    onChange={handleEditChange}
                  >
                    <option value="Work">Work</option>
                    <option value="Personal">Personal</option>
                    <option value="Shopping">Shopping</option>
                    <option value="Health">Health</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Status</label>
                  <select
                    name="status"
                    value={editTask.status || 'Pending'}
                    onChange={handleEditChange}
                  >
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

              {allUsers.length > 0 && (
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
              )}

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
    </div>
  );
}

export default AdminPanel;
