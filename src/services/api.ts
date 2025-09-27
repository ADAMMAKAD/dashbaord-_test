export interface Order {
  id: number;
  user: {
    id: number;
    username: string;
    email?: string;
    role: string;
  };
  product: {
    id: number;
    name: string;
    description: string;
    price: number;
    category?: string;
    stock: number;
  };
  quantity: number;
  total: number;
  orderDate: string;
}

export interface ApiResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
}

export interface ApiFilters {
  search?: string;
  user?: string;
  product?: string;
}

const API_BASE_URL = 'http://54.87.221.209:8080/api';

// Authentication interfaces
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username?: string;
  role?: string;
}

export interface User {
  id: number;
  username: string;
  email?: string;
  role: string;
}

export const getAuthToken = (): string | null => {
  return localStorage.getItem('authToken');
};

export const setAuthToken = (token: string): void => {
  localStorage.setItem('authToken', token);
};

export const removeAuthToken = (): void => {
  localStorage.removeItem('authToken');
};

const createAuthHeaders = (): HeadersInit => {
  const token = getAuthToken();
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
};

export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Login failed');
    }

    const data = await response.json();
    return data;
  } catch (error) {
    throw error;
  }
};

export const logout = (): void => {
  removeAuthToken();
};

export const fetchOrders = async (
  page: number = 1,
  limit: number = 10,
  filters: ApiFilters = {}
): Promise<ApiResponse<Order>> => {
  try {
    const response = await fetch(`${API_BASE_URL}/orders`, {
      method: 'GET',
      headers: createAuthHeaders(),
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const allOrders: Order[] = await response.json();
    
    let filteredOrders = allOrders;
    
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase();
      filteredOrders = filteredOrders.filter(order => 
        order.user.username.toLowerCase().includes(searchTerm) ||
        order.product.name.toLowerCase().includes(searchTerm)
      );
    }
    if (filters.user) {
      filteredOrders = filteredOrders.filter(order => 
        order.user.username.toLowerCase().includes(filters.user!.toLowerCase())
      );
    }
    
    if (filters.product) {
      filteredOrders = filteredOrders.filter(order => 
        order.product.name.toLowerCase().includes(filters.product!.toLowerCase())
      );
    }
    
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + limit;
    const paginatedOrders = filteredOrders.slice(startIndex, endIndex);
    
    return {
      data: paginatedOrders,
      total: filteredOrders.length,
      page,
      limit
    };
  } catch (error) {
    throw error;
  }
};

export const getOrderStats = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/orders`, {
      method: 'GET',
      headers: createAuthHeaders(),
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const orders: Order[] = await response.json();
    
    const totalOrders = orders.length;
    const totalRevenue = orders.reduce((sum, order) => sum + order.total, 0);
    const uniqueUsers = new Set(orders.map(order => order.user.id)).size;
    const avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
    
    return {
      totalOrders,
      totalRevenue,
      uniqueUsers,
      avgOrderValue: Math.round(avgOrderValue * 100) / 100
    };
  } catch (error) {
    return {
      totalOrders: 0,
      totalRevenue: 0,
      uniqueUsers: 0,
      avgOrderValue: 0
    };
  }
};