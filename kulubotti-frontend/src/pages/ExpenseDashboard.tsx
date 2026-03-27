import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { Plus, Receipt, Clock, CheckCircle, Loader2, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface Expense {
  id: string;
  merchantName: string;
  amount: number;
  status: 'PENDING' | 'PROCESSED';
  date: string;
}

const ExpenseDashboard = () => {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const { logout } = useAuth();

  // 1. Fetch Expenses from the Java Expense Service
  const fetchExpenses = async () => {
    try {
      const response = await api.get('/expenses');
      setExpenses(response.data);
    } catch (err) {
      console.error("Failed to fetch expenses", err);
    } finally {
      setLoading(false);
    }
  };

  // 2. Initial load + Polling logic (Every 3 seconds)
  useEffect(() => {
    fetchExpenses();
    const interval = setInterval(fetchExpenses, 3000);
    return () => clearInterval(interval); // Clean up on close
  }, []);

  const handleAddTestExpense = async () => {
    try {
      // Sending a test payload to trigger the Kafka pipeline
      await api.post('/expenses', {
        merchantName: "Reaktor Cafe",
        amount: 5.50,
        date: new Date().toISOString().split('T')[0]
      });
      fetchExpenses(); // Refresh list to show the PENDING state
    } catch (err) {
      alert("Error adding expense");
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-start mb-12 bg-slate-900/50 p-6 rounded-2xl border border-white/5 backdrop-blur-md">
          <div>
            <h1 className="text-4xl font-black tracking-tight bg-gradient-to-r from-blue-400 via-indigo-400 to-emerald-400 bg-clip-text text-transparent">
              KuluBotti <span className="text-sm font-light text-slate-500 ml-2">v4.0</span>
            </h1>
            <p className="text-slate-400 mt-1 font-medium">Monitoring your financial pulse.</p>
          </div>

          <div className="flex items-center gap-6">
            <button
              onClick={handleAddTestExpense}
              className="group relative flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-6 py-3 rounded-xl font-bold transition-all hover:scale-105 active:scale-95 shadow-lg shadow-blue-500/20"
            >
              <Plus size={20} className="group-hover:rotate-90 transition-transform" />
              New Receipt
            </button>

            {/* Separator Line */}
            <div className="h-10 w-[1px] bg-slate-800"></div>

            <button
              onClick={logout}
              className="flex flex-col items-center text-slate-500 hover:text-red-400 transition-colors group"
              title="Secure Logout"
            >
              <LogOut size={22} />
              <span className="text-[10px] mt-1 uppercase tracking-widest font-bold opacity-0 group-hover:opacity-100 transition-opacity">Exit</span>
            </button>
          </div>
        </div>

        {/* Expense List */}
        <div className="grid gap-4">
          {loading ? (
            <div className="flex justify-center p-12"><Loader2 className="animate-spin text-blue-500" size={40} /></div>
          ) : expenses.map((exp) => (
            <div key={exp.id} className="bg-slate-900 border border-slate-800 p-5 rounded-xl flex items-center justify-between hover:border-slate-700 transition-all shadow-lg">
              <div className="flex items-center gap-4">
                <div className="bg-slate-800 p-3 rounded-lg">
                  <Receipt className="text-blue-400" />
                </div>
                <div>
                  <h3 className="font-bold text-lg">{exp.merchantName}</h3>
                  <p className="text-sm text-slate-500">{exp.date}</p>
                </div>
              </div>

              <div className="flex items-center gap-8">
                <div className="text-right">
                  <p className="text-xl font-mono font-bold">€{exp.amount.toFixed(2)}</p>
                  <div className="flex items-center gap-1 justify-end mt-1">
                    {exp.status === 'PENDING' ? (
                      <span className="text-amber-400 text-xs flex items-center gap-1">
                        <Clock size={12} className="animate-pulse" /> AI Processing...
                      </span>
                    ) : (
                      <span className="text-emerald-400 text-xs flex items-center gap-1">
                        <CheckCircle size={12} /> Processed
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ExpenseDashboard;