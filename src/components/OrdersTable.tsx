import React from 'react';
import { Order } from '../services/api';

interface OrdersTableProps {
  orders: Order[];
  loading: boolean;
}

const OrdersTable = ({ orders, loading }: OrdersTableProps) => {
  const formatDt = (dateString: string) => { 
    const dt = new Date(dateString); 
    return dt.toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="orders-table-container">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading orders...</p>
        </div>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="orders-table-container">
        <div className="no-orders">
          <h3>No orders found</h3>
          <p>Try adjusting your search criteria.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="orders-table-container">
      <table className="orders-table">
        <thead>
          <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Product</th>
            <th>Quantity</th>
            <th>Price</th>
            <th>Total</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => ( 
            <tr key={o.id}>
              <td>#{o.id}</td>
              <td>{o.user.username}</td>
              <td>{o.product.name}</td>
              <td>{o.quantity}</td>
              <td>${o.product.price.toFixed(2)}</td>
              <td>${o.total.toFixed(2)}</td>
              <td>{formatDt(o.orderDate)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default OrdersTable;