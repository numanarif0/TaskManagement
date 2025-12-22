import { useState, useEffect } from 'react';
import { attachmentService } from '../services/api';
import './FileManager.css';

function FileManager({ taskId, taskTitle, onClose }) {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);

  // Desteklenen dosya formatları
  const ALLOWED_TYPES = [
    'application/pdf',
    'image/png',
    'image/jpeg',
    'image/jpg',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // docx
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' // xlsx
  ];
  const ALLOWED_EXTENSIONS = ['pdf', 'png', 'jpg', 'jpeg', 'docx', 'xlsx'];
  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

  useEffect(() => {
    fetchFiles();
  }, [taskId]);

  const fetchFiles = async () => {
    setLoading(true);
    const result = await attachmentService.getFilesByTask(taskId);
    setLoading(false);
    if (result.success) {
      setFiles(result.data || []);
    } else {
      setError('Failed to load files');
    }
  };

  const validateFile = (file) => {
    const extension = file.name.split('.').pop().toLowerCase();
    
    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      return `Invalid file type. Allowed: ${ALLOWED_EXTENSIONS.join(', ')}`;
    }
    
    if (file.size > MAX_FILE_SIZE) {
      return 'File size exceeds 10MB limit';
    }
    
    return null;
  };

  const handleFileUpload = async (fileList) => {
    if (!fileList || fileList.length === 0) return;

    const file = fileList[0];
    const validationError = validateFile(file);
    
    if (validationError) {
      setError(validationError);
      return;
    }

    setError('');
    setUploading(true);
    
    const result = await attachmentService.uploadFile(taskId, file);
    setUploading(false);
    
    if (result.success) {
      setFiles(prev => [...prev, result.data]);
    } else {
      setError(result.error || 'Failed to upload file');
    }
  };

  const handleDownload = async (attachment) => {
    const result = await attachmentService.downloadFile(attachment.id, attachment.fileName);
    if (!result.success) {
      setError('Failed to download file');
    }
  };

  const handleDelete = async (attachmentId) => {
    const confirmed = window.confirm('Are you sure you want to delete this file?');
    if (!confirmed) return;

    const result = await attachmentService.deleteFile(attachmentId);
    if (result.success) {
      setFiles(prev => prev.filter(f => f.id !== attachmentId));
    } else {
      setError('Failed to delete file');
    }
  };

  // Drag & Drop handlers
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    handleFileUpload(e.dataTransfer.files);
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('tr-TR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getFileIcon = (fileName) => {
    const ext = fileName?.split('.').pop().toLowerCase();
    const icons = {
      pdf: '📄',
      png: '🖼️',
      jpg: '🖼️',
      jpeg: '🖼️',
      docx: '📝',
      xlsx: '📊'
    };
    return icons[ext] || '📎';
  };

  const getDisplayName = (file) => {
    return file.originalFileName || file.fileName;
  };

  return (
    <div className="file-manager-overlay" onClick={onClose}>
      <div className="file-manager-modal" onClick={(e) => e.stopPropagation()}>
        <div className="file-manager-header">
          <div>
            <h2>📎 Attachments</h2>
            <p className="task-title-label">Task: {taskTitle}</p>
          </div>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        {error && (
          <div className="error-message">
            ⚠️ {error}
            <button onClick={() => setError('')}>×</button>
          </div>
        )}

        {/* Upload Area */}
        <div
          className={`upload-area ${dragActive ? 'drag-active' : ''}`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          <input
            type="file"
            id="file-input"
            accept=".pdf,.png,.jpg,.jpeg,.docx,.xlsx"
            onChange={(e) => handleFileUpload(e.target.files)}
            disabled={uploading}
            hidden
          />
          <label htmlFor="file-input" className="upload-label">
            {uploading ? (
              <div className="uploading">
                <span className="spinner"></span>
                <span>Uploading...</span>
              </div>
            ) : (
              <>
                <span className="upload-icon">📁</span>
                <span className="upload-text">
                  Drag & drop a file here, or <strong>click to browse</strong>
                </span>
                <span className="upload-hint">
                  Allowed: PDF, PNG, JPG, DOCX, XLSX (Max 10MB)
                </span>
              </>
            )}
          </label>
        </div>

        {/* Files List */}
        <div className="files-section">
          <h3>Uploaded Files ({files.length})</h3>
          
          {loading ? (
            <div className="loading-state">Loading files...</div>
          ) : files.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">📭</span>
              <p>No files attached to this task yet.</p>
            </div>
          ) : (
            <div className="files-list">
              {files.map((file) => (
                <div key={file.id} className="file-item">
                  <div className="file-icon">{getFileIcon(getDisplayName(file))}</div>
                  <div className="file-info">
                    <span className="file-name">{getDisplayName(file)}</span>
                    <span className="file-meta">
                      {formatFileSize(file.fileSize)} • {formatDate(file.uploadDate)}
                    </span>
                  </div>
                  <div className="file-actions">
                    <button
                      className="btn-download"
                      onClick={() => handleDownload({ ...file, fileName: getDisplayName(file) })}
                      title="Download"
                    >
                      ⬇️
                    </button>
                    <button
                      className="btn-delete-file"
                      onClick={() => handleDelete(file.id)}
                      title="Delete"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default FileManager;
