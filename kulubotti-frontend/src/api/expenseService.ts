import api from './axiosConfig';

export interface Expense {
  id: string;
  merchantName: string;
  amount: number;
  status: 'PENDING' | 'PROCESSED';
  date: string;
}

export const expenseService = {
  // Fetch all expenses
  getExpenses: async (): Promise<Expense[]> => {
    const response = await api.get('/expenses');
    return response.data;
  },

  // Upload the raw image file
  uploadReceipt: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/expenses/upload', formData);
    return response.data;
  }
};