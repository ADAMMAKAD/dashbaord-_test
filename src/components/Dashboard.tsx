import React, { useState, useEffect } from 'react';
import { fetchOrders, getOrderStats, Order, ApiFilters } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import OrdersTable from './OrdersTable';
import SearchBar from './SearchBar';
import FilterPanel from './FilterPanel';
import Pagination from './Pagination';
import Charts from './Charts';

// Main dashboard component - shows orders and stats
//  split this into smaller components later
const Dashboard = () => {
  const { user, logout } = useAuth();
  
  // State for orders data
  const [orders, setOrders] = useState<Order[]>([]);
  const [allOrders, setAllOrders] = useState<Order[]>([]); // keep all orders for local filtering
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Search and filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState<ApiFilters>({});
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  
  // Stats for the dashboard cards
  const [stats, setStats] = useState({
    totalOrders: 0,
    totalRevenue: 0,
    uniqueUsers: 0, // quick fix - match API response
    avgOrderValue: 0
  });
  const [statsLoading, setStatsLoading] = useState(false);
  const itemsPerPage = 10; // make this configurable
  
  // quick hack for refresh - will improve later
  const [refreshKey, setRefreshKey] = useState(0);

  // Load orders with current filters and pagination
  const loadOrders = async () => {
    setLoading(true);
    setError(null); // clear previous errors
    
    try {
      // Combine search and filters for API call
      const apiFilters: ApiFilters = {
        search: searchTerm,
        ...filters
      };
      
      const response = await fetchOrders(currentPage, itemsPerPage, apiFilters);
      setOrders(response.data);
      setTotalItems(response.total);
    } catch (error) {
      setError('Failed to load orders. Please try again.');
      if (orders.length === 0) {
        setOrders([]);
      }
    } finally {
      setLoading(false);
    }
  };

  const loadAllOrders = async () => {
    try {
      const response = await fetchOrders(1, 1000);
      setAllOrders(response.data);
    } catch (err) {
      // Charts are not critical
    }
  };

  const loadStats = async () => {
    setStatsLoading(true);
    try {
      const statsData = await getOrderStats();
      setStats(statsData);
    } catch (err) {
      setStats({
        totalOrders: 0,
        totalRevenue: 0,
        uniqueUsers: 0,
        avgOrderValue: 0
      });
    } finally {
      setStatsLoading(false);
    }
  };

  const quickRefresh = () => {
    setRefreshKey(prev => prev + 1);
    loadOrders();
    loadStats();
  };

  useEffect(() => {
    loadOrders();
    loadStats();
    if (allOrders.length === 0) {
      loadAllOrders();
    }
  }, [currentPage, searchTerm, filters, refreshKey]);

  const handleSearch = (term: string) => {
    setSearchTerm(term);
    setCurrentPage(1);
  };

  const handleFilterChange = (newFilters: { user?: string; product?: string }) => {
    setFilters(newFilters);
    setCurrentPage(1); // reset to first page when filtering
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    // scroll to top - quick fix
    window.scrollTo(0, 0);
  };

  const handleLimitChange = (newLimit: number) => {
    // TODO: implement limit change properly
    setCurrentPage(1);
  };

  const handleRetry = () => {
    setError(null);
    loadOrders();
  };

  // Show error state if there's an error and no data
  if (error && orders.length === 0) {
    return (
      <div className="dashboard">
        <div className="dashboard-header">
          <div className="header-content">
            <h1>Order Dashboard</h1>
            <div className="user-info">
              <span className="welcome-text">Welcome, {user?.username}</span>
              <span className="user-role">{user?.role}</span>
              <button onClick={logout} className="logout-button">
                Logout
              </button>
            </div>
          </div>
        </div>
        
        <div className="error">
          <h2>Oops! Something went wrong</h2>
          <p>{error}</p>
          <button onClick={handleRetry}>Try Again</button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div className="header-content">
          <h1>Order Dashboard</h1>
          <div className="user-info">
            <span className="welcome-text">Welcome, {user?.username}</span>
            <span className="user-role">{user?.role}</span>
            {/* quick refresh button - temporary */}
            <button onClick={quickRefresh} style={{marginRight: '10px', fontSize: '12px'}}>
              🔄
            </button>
            <button onClick={logout} className="logout-button">
              Logout
            </button>
          </div>
        </div>
      </div>
      
      {/* Show error banner if there's an error but we have existing data */}
      {error && orders.length > 0 && (
        <div className="error-banner" style={{
          background: '#fff3cd',
          border: '1px solid #ffeaa7',
          color: '#856404',
          padding: '10px',
          borderRadius: '4px',
          marginBottom: '20px'
        }}>
          {error} <button onClick={handleRetry} style={{marginLeft: '10px'}}>Retry</button>
        </div>
      )}
      
      <div className="stats">
        <div className="stat-card">
          <h3>Total Orders</h3>
          <p>{statsLoading ? '...' : stats.totalOrders}</p>
        </div>
        <div className="stat-card">
          <h3>Total Revenue</h3>
          <p>{statsLoading ? '...' : `$${stats.totalRevenue.toFixed(2)}`}</p>
        </div>
        <div className="stat-card">
          <h3>Customers</h3>
          <p>{statsLoading ? '...' : stats.uniqueUsers}</p>
        </div>
        <div className="stat-card">
          <h3>Avg Order Value</h3>
          <p>{statsLoading ? '...' : `$${stats.avgOrderValue.toFixed(2)}`}</p>
        </div>
      </div>

      <Charts orders={allOrders} />

      <div className="controls-section">
        <SearchBar 
          onSearch={handleSearch} 
          initialValue={searchTerm}
        />
        
        <FilterPanel 
          filters={filters} 
          onFilterChange={handleFilterChange}
          orders={allOrders}
        />
      </div>

      <OrdersTable orders={orders} loading={loading} />
      
      <Pagination 
        currentPage={currentPage}
        totalItems={totalItems}
        itemsPerPage={itemsPerPage}
        onPageChange={handlePageChange}
        onLimitChange={handleLimitChange}
      />
    </div>
  );
};

export default Dashboard;