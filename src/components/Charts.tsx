import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { Order } from '../services/api';

interface ChartsProps {
  orders: Order[];
}

const Charts = ({ orders }: ChartsProps) => {
  // group products by sales count
  const productSales = orders.reduce((acc, order) => {
    const prodName = order.product.name;
    if (!acc[prodName]) {
      acc[prodName] = 0;
    }
    acc[prodName] += order.quantity;
    return acc;
  }, {} as Record<string, number>);

  // count how many orders each customer has made
  const customerOrders = orders.reduce((acc, order) => {
    const username = order.user.username;
    acc[username] = (acc[username] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  // convert to chart format
  const chartData = Object.entries(productSales).map(([product, qty]) => ({
    name: product,
    count: qty
  }));

  const customerData = Object.entries(customerOrders).map(([customer, orderCount]) => ({
    name: customer,
    count: orderCount
  }));

  // some nice colors for the charts
  const colors = ['#D63C0D', '#00AAC4', '#14094A', '#FF8042', '#8DD1E1'];

  return (
    <div className="charts-container">
      <div className="chart-item">
        <h3>Product Sales Volume</h3>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="count" fill="#D63C0D" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="chart-item">
        <h3>Customer Order Distribution</h3>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={customerData}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={(entry: any) => `${entry.name} (${(entry.percent * 100).toFixed(0)}%)`}
              outerRadius={80}
              fill="#8884d8"
              dataKey="count"
            >
              {customerData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default Charts;