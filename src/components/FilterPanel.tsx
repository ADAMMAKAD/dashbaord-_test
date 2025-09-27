import React from 'react';

interface FilterPanelProps {
  filters: {
    user?: string;
    product?: string;
  };
  onFilterChange: (filters: { user?: string; product?: string }) => void;
  orders: Array<{
    user: { username: string };
    product: { name: string };
  }>;
}

const FilterPanel = ({ filters, onFilterChange, orders }: FilterPanelProps) => {
  const uniqueUsers = Array.from(new Set(orders.map(order => order.user.username)));
  const uniqueProducts = Array.from(new Set(orders.map(order => order.product.name)));

  const handleUserChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    onFilterChange({
      ...filters,
      user: value === '' ? undefined : value
    });
  };

  const handleProductChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    onFilterChange({
      ...filters,
      product: value === '' ? undefined : value
    });
  };

  const clearFilters = () => {
    onFilterChange({});
  };

  return (
    <div className="filter-panel">
      <div className="filter-group">
        <label htmlFor="user-filter">Filter by User:</label>
        <select 
          id="user-filter" 
          value={filters.user || ''} 
          onChange={handleUserChange}
        >
          <option value="">All Users</option>
          {uniqueUsers.map(user => (
            <option key={user} value={user}>{user}</option>
          ))}
        </select>
      </div>

      <div className="filter-group">
        <label htmlFor="product-filter">Filter by Product:</label>
        <select 
          id="product-filter" 
          value={filters.product || ''} 
          onChange={handleProductChange}
        >
          <option value="">All Products</option>
          {uniqueProducts.map(product => (
            <option key={product} value={product}>{product}</option>
          ))}
        </select>
      </div>

      {(filters.user || filters.product) && (
        <button onClick={clearFilters} className="clear-filters">
          Clear Filters
        </button>
      )}
    </div>
  );
};

export default FilterPanel;