import { useState, useEffect, useMemo } from 'react';
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js';
import { Pie, Bar, Doughnut } from 'react-chartjs-2';
import './Statistics.css';

// Chart.js kayıt
ChartJS.register(
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

function Statistics({ tasks, onClose }) {
  // Kategori bazlı task sayıları
  const categoryData = useMemo(() => {
    const categories = {};
    tasks.forEach(task => {
      const cat = task.category || 'Other';
      categories[cat] = (categories[cat] || 0) + 1;
    });
    return categories;
  }, [tasks]);

  // Durum bazlı task sayıları
  const statusData = useMemo(() => {
    const statuses = { 'Completed': 0, 'In Progress': 0, 'Pending': 0 };
    tasks.forEach(task => {
      const status = task.status || 'Pending';
      statuses[status] = (statuses[status] || 0) + 1;
    });
    return statuses;
  }, [tasks]);

  // Kategori bazlı tamamlanma durumu
  const categoryCompletionData = useMemo(() => {
    const data = {};
    tasks.forEach(task => {
      const cat = task.category || 'Other';
      if (!data[cat]) {
        data[cat] = { completed: 0, incomplete: 0 };
      }
      if (task.status === 'Completed') {
        data[cat].completed += 1;
      } else {
        data[cat].incomplete += 1;
      }
    });
    return data;
  }, [tasks]);

  // Renk paleti
  const colors = {
    completed: '#4caf50',
    inProgress: '#ff9800',
    pending: '#2196f3',
    categories: ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe']
  };

  // Status Pie Chart verileri
  const statusChartData = {
    labels: ['Completed', 'In Progress', 'Pending'],
    datasets: [{
      data: [statusData['Completed'], statusData['In Progress'], statusData['Pending']],
      backgroundColor: [colors.completed, colors.inProgress, colors.pending],
      borderColor: ['#fff', '#fff', '#fff'],
      borderWidth: 2,
    }]
  };

  // Category Doughnut Chart verileri
  const categoryChartData = {
    labels: Object.keys(categoryData),
    datasets: [{
      data: Object.values(categoryData),
      backgroundColor: colors.categories.slice(0, Object.keys(categoryData).length),
      borderColor: '#fff',
      borderWidth: 2,
    }]
  };

  // Kategori bazlı tamamlanma durumu Bar Chart
  const categoryCompletionChartData = {
    labels: Object.keys(categoryCompletionData),
    datasets: [
      {
        label: 'Completed',
        data: Object.values(categoryCompletionData).map(d => d.completed),
        backgroundColor: colors.completed,
        borderRadius: 4,
      },
      {
        label: 'Incomplete',
        data: Object.values(categoryCompletionData).map(d => d.incomplete),
        backgroundColor: '#e0e0e0',
        borderRadius: 4,
      }
    ]
  };

  const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'Tasks by Category (Completed vs Incomplete)',
        font: { size: 16 }
      }
    },
    scales: {
      x: {
        stacked: true,
      },
      y: {
        stacked: true,
        beginAtZero: true,
        ticks: {
          stepSize: 1
        }
      }
    }
  };

  const pieOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
      }
    }
  };

  // Özet istatistikler
  const totalTasks = tasks.length;
  const completedTasks = statusData['Completed'];
  const completionRate = totalTasks > 0 ? ((completedTasks / totalTasks) * 100).toFixed(1) : 0;

  return (
    <div className="statistics-overlay" onClick={onClose}>
      <div className="statistics-modal" onClick={(e) => e.stopPropagation()}>
        <div className="statistics-header">
          <h2>📊 Task Statistics</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        {/* Özet Kartlar */}
        <div className="stats-summary">
          <div className="summary-card">
            <span className="summary-number">{totalTasks}</span>
            <span className="summary-label">Total Tasks</span>
          </div>
          <div className="summary-card completed">
            <span className="summary-number">{completedTasks}</span>
            <span className="summary-label">Completed</span>
          </div>
          <div className="summary-card">
            <span className="summary-number">{completionRate}%</span>
            <span className="summary-label">Completion Rate</span>
          </div>
        </div>

        {/* Grafikler */}
        <div className="charts-grid">
          {/* Status Pie Chart */}
          <div className="chart-container">
            <h3>Status Distribution</h3>
            <div className="chart-wrapper">
              <Pie data={statusChartData} options={pieOptions} />
            </div>
          </div>

          {/* Category Doughnut Chart */}
          <div className="chart-container">
            <h3>Tasks by Category</h3>
            <div className="chart-wrapper">
              <Doughnut data={categoryChartData} options={pieOptions} />
            </div>
          </div>

          {/* Category Completion Bar Chart */}
          <div className="chart-container full-width">
            <div className="chart-wrapper bar-chart">
              <Bar data={categoryCompletionChartData} options={barOptions} />
            </div>
          </div>
        </div>

        {/* Kategori Detayları */}
        <div className="category-breakdown">
          <h3>Category Breakdown</h3>
          <div className="breakdown-list">
            {Object.entries(categoryCompletionData).map(([category, data]) => (
              <div key={category} className="breakdown-item">
                <span className="breakdown-category">{category}</span>
                <div className="breakdown-bar">
                  <div 
                    className="breakdown-completed" 
                    style={{ 
                      width: `${(data.completed / (data.completed + data.incomplete)) * 100}%` 
                    }}
                  />
                </div>
                <span className="breakdown-stats">
                  {data.completed}/{data.completed + data.incomplete} completed
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Statistics;
